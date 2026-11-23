import Foundation
import StoreKit
import Shared

/**
 * StoreKit 2 for the shared Kotlin billing code (IosBillingRepository).
 * Transactions are finished only after the IAP backend accepted them (Kotlin calls finish(transactionId:)).
 */
final class StoreKitBridgeImpl: NSObject, StoreKitBridge {

    private var products: [String: Product] = [:]
    private var unfinished: [String: Transaction] = [:]
    private var updatesTask: Task<Void, Never>?

    func loadProducts(productIds: [String], completion: @escaping ([StoreProduct]) -> Void) {
        Task {
            do {
                let loaded = try await Product.products(for: productIds)
                loaded.forEach { products[$0.id] = $0 }
                completion(loaded.map {
                    StoreProduct(id: $0.id, displayName: $0.displayName, displayPrice: $0.displayPrice,
                                 isSubscription: $0.type == .autoRenewable)
                })
            } catch {
                completion([])
            }
        }
    }

    func purchase(productId: String, appAccountToken: String?, completion: @escaping (StorePurchaseResult) -> Void) {
        Task {
            do {
                if products[productId] == nil {
                    try await Product.products(for: [productId]).forEach { products[$0.id] = $0 }
                }
                guard let product = products[productId] else {
                    completion(StorePurchaseResult(status: "failed", transaction: nil, error: "This plan is not available in the App Store."))
                    return
                }
                var options: Set<Product.PurchaseOption> = []
                if let token = appAccountToken, let uuid = UUID(uuidString: token) {
                    options.insert(.appAccountToken(uuid))
                }
                switch try await product.purchase(options: options) {
                case .success(let verification):
                    switch verification {
                    case .verified(let transaction):
                        completion(StorePurchaseResult(status: "success", transaction: storeTransaction(transaction, jws: verification.jwsRepresentation), error: nil))
                    case .unverified(_, let error):
                        completion(StorePurchaseResult(status: "failed", transaction: nil, error: error.localizedDescription))
                    }
                case .pending:
                    completion(StorePurchaseResult(status: "pending", transaction: nil, error: nil))
                case .userCancelled:
                    completion(StorePurchaseResult(status: "cancelled", transaction: nil, error: nil))
                @unknown default:
                    completion(StorePurchaseResult(status: "failed", transaction: nil, error: nil))
                }
            } catch {
                completion(StorePurchaseResult(status: "failed", transaction: nil, error: error.localizedDescription))
            }
        }
    }

    func currentEntitlements(completion: @escaping ([StoreTransaction]) -> Void) {
        Task {
            var owned: [StoreTransaction] = []
            for await result in Transaction.currentEntitlements {
                if case .verified(let transaction) = result {
                    owned.append(storeTransaction(transaction, jws: result.jwsRepresentation))
                }
            }
            completion(owned)
        }
    }

    func syncWithAppStore(completion: @escaping (KotlinBoolean) -> Void) {
        Task {
            do {
                try await AppStore.sync()
                completion(KotlinBoolean(bool: true))
            } catch {
                completion(KotlinBoolean(bool: false))
            }
        }
    }

    func finish(transactionId: String) {
        guard let transaction = unfinished.removeValue(forKey: transactionId) else { return }
        Task { await transaction.finish() }
    }

    func observeTransactionUpdates(onTransaction: @escaping (StoreTransaction) -> Void) {
        updatesTask?.cancel()
        updatesTask = Task.detached { [weak self] in
            for await result in Transaction.updates {
                guard let self, case .verified(let transaction) = result else { continue }
                onTransaction(self.storeTransaction(transaction, jws: result.jwsRepresentation))
            }
        }
    }

    private func storeTransaction(_ transaction: Transaction, jws: String) -> StoreTransaction {
        unfinished[String(transaction.id)] = transaction
        return StoreTransaction(
            productId: transaction.productID,
            transactionId: String(transaction.id),
            originalTransactionId: String(transaction.originalID),
            signedTransaction: jws,
            isSubscription: transaction.productType == .autoRenewable
        )
    }
}
