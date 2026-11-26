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
    /// products/unfinished are touched from several StoreKit tasks at once (restore on launch, Transaction.updates,
    /// purchases); unguarded concurrent writes corrupted the dictionaries and crashed the app on open
    private let lock = NSLock()
    private func withLock<T>(_ body: () -> T) -> T { lock.lock(); defer { lock.unlock() }; return body() }

    func loadProducts(productIds: [String], completion: @escaping ([StoreProduct]) -> Void) {
        Task {
            do {
                let loaded = try await Product.products(for: productIds)
                withLock { loaded.forEach { products[$0.id] = $0 } }
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
                if withLock({ products[productId] }) == nil {
                    let fetched = try await Product.products(for: [productId])
                    withLock { fetched.forEach { products[$0.id] = $0 } }
                }
                guard let product = withLock({ products[productId] }) else {
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
                        completion(StorePurchaseResult(status: "success", transaction: await storeTransaction(transaction, jws: verification.jwsRepresentation), error: nil))
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
                    owned.append(await storeTransaction(transaction, jws: result.jwsRepresentation))
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
        guard let transaction = withLock({ unfinished.removeValue(forKey: transactionId) }) else { return }
        Task { await transaction.finish() }
    }

    func observeTransactionUpdates(onTransaction: @escaping (StoreTransaction) -> Void) {
        updatesTask?.cancel()
        updatesTask = Task.detached { [weak self] in
            for await result in Transaction.updates {
                guard let self, case .verified(let transaction) = result else { continue }
                onTransaction(await self.storeTransaction(transaction, jws: result.jwsRepresentation))
            }
        }
    }

    /// The transaction for Kotlin, with the subscription's renewal-info JWS (the backend reads pending downgrades from it).
    private func storeTransaction(_ transaction: Transaction, jws: String) async -> StoreTransaction {
        withLock { unfinished[String(transaction.id)] = transaction }
        var renewalJws: String? = nil
        if #available(iOS 16.4, *), transaction.productType == .autoRenewable,
           let status = try? await transaction.subscriptionStatus, case .verified = status.renewalInfo {
            renewalJws = status.renewalInfo.jwsRepresentation
        }
        return StoreTransaction(
            productId: transaction.productID,
            transactionId: String(transaction.id),
            originalTransactionId: String(transaction.originalID),
            signedTransaction: jws,
            signedRenewalInfo: renewalJws,
            isSubscription: transaction.productType == .autoRenewable
        )
    }
}
