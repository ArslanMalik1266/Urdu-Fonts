import UIKit
import Shared

// TEMP (local only): real ads when the WebsCareAds pod is installed, a no-ads stand-in otherwise.
#if canImport(WebsCareAds)
import WebsCareAds

/**
 * WebsCareAds 4.2.5 for iOS (the XCFramework from the release ZIP, linked by the Podfile) for the shared Kotlin
 * AdManager (IosAdManager). Same placements and rules as the Android WebsCareAdManagerImpl:
 * - init once on the main thread, TEST environment for debug builds, PRODUCTION for release (still gated by the
 *   remote policy's test_mode until the owner enables live inventory)
 * - consent (UMP) before any ad; a visible privacy-options entry when required
 * - premium users get no ads; app-open only while the splash is on screen; rewards only from the SDK callback.
 *
 * Both this app's Kotlin framework (Shared) and WebsCareAds are Kotlin/Native frameworks, so their boxed
 * types are written with the module name (Shared.KotlinBoolean vs WebsCareAds.KotlinBoolean).
 */
final class AdsBridgeImpl: NSObject, AdsBridge {

    private let resumePlacement = "urdufonts_app_resume"
    private let splashWaitMs: Int64 = 6_000 // WebsCareAds' DEFAULT_APP_OPEN_WAIT_MS
    /** Banner/native slots still waiting for an ad, by placement: (onLoaded, onFailed), oldest first. */
    private var pending: [String: [(container: UIView, loaded: (Shared.KotlinDouble) -> Void, failed: () -> Void)]] = [:]

    func initialize(isDebug: Bool) {
        WebsCareAds.shared.doInit { config in
            config.environment = isDebug ? AdEnvironment.test : AdEnvironment.production
            // WebsCareAds iOS 4.2.5 reads the native template size once, here, for the whole app (no per-slot or
            // per-placement size, unlike Android). SMALL matches Home/Styles/Font list; Categories (MEDIUM on
            // Android) also gets SMALL until the SDK supports a size per load.
            config.defaultNativeSize = NativeSize.small
            config.onAdFailed = { [weak self] adType, placement, _, message in
                DispatchQueue.main.async { self?.slotFailed(placement) }
                if isDebug { print("WebsCareAds onAdFailed: \(adType) '\(placement)' -> \(message)") }
            }
            // Refused before any request (policy not ready, consent, OFF, premium...): treat as a failure so the slot collapses
            config.onAdError = { [weak self] adType, placement, error in
                DispatchQueue.main.async { self?.slotFailed(placement) }
                if isDebug { print("WebsCareAds onAdError: \(adType) '\(placement)' -> \(error)") }
            }
            config.onAdLoaded = { [weak self] adType, placement, _ in
                if isDebug { print("WebsCareAds onAdLoaded: \(adType) '\(placement)'") }
                DispatchQueue.main.async { self?.slotLoaded(placement) }
            }
        }
        // Return-to-app ad on its own placement, as on Android
        WebsCareAds.shared.enableAutoAppOpen(placement: resumePlacement, minBackgroundSeconds: 30)
    }

    func setPremium(isPremium: Bool) {
        WebsCareAds.shared.setPremium(enabled: isPremium)
    }

    func requestConsent(completion: @escaping (Shared.KotlinBoolean) -> Void) {
        WebsCareAds.shared.requestConsent { gathered in
            completion(Shared.KotlinBoolean(bool: gathered.boolValue))
        }
    }

    func isPrivacyOptionsRequired() -> Bool {
        WebsCareAds.shared.isPrivacyOptionsRequired()
    }

    func showPrivacyOptions(completion: @escaping () -> Void) {
        WebsCareAds.shared.showConsentForm(onComplete: completion)
    }

    func preload(kind: String, placement: String) {
        switch kind {
        case "app_open": WebsCareAds.shared.preloadAppOpen(placement: placement)
        case "interstitial": WebsCareAds.shared.preloadInterstitial(placement: placement)
        case "rewarded": WebsCareAds.shared.preloadRewarded(placement: placement)
        default: break
        }
    }

    func showSplashAppOpen(placement: String, onContinue: @escaping () -> Void) {
        // As on Android: inside checkConsent; onDismissed runs exactly once on every path (shown, no fill,
        // timeout, premium, consent), and navigation happens only there
        WebsCareAds.shared.checkConsent(viewController: topViewController) {
            WebsCareAds.shared.loadAndShowAppOpen(
                placement: placement,
                timeoutMs: self.splashWaitMs,
                onFailed: { error in print("Splash app-open not shown: \(error)") },
                onDismissed: onContinue
            )
        }
    }

    func showInterstitial(placement: String, onDismissed: @escaping () -> Void) {
        WebsCareAds.shared.showInterstitial(placement: placement, onDismissed: onDismissed)
    }

    func showRewarded(placement: String, onRewarded: @escaping (String, Shared.KotlinInt) -> Void, onNotReady: @escaping () -> Void) {
        WebsCareAds.shared.showRewarded(
            placement: placement,
            onRewarded: { type, amount in onRewarded(type, Shared.KotlinInt(int: amount.int32Value)) },
            onDismissed: {},
            onNotReady: onNotReady
        )
    }

    func setAppOpenLoadingScreenVisible(visible: Bool) {
        WebsCareAds.shared.setAppOpenLoadingScreenVisible(visible: visible)
    }

    func loadBanner(container: UIView, placement: String, onLoaded: @escaping (Shared.KotlinDouble) -> Void, onFailed: @escaping () -> Void) {
        pending[placement, default: []].append((container, onLoaded, onFailed))
        prepare(container, height: 60)
        // As in the SDK sample: inline loads only after consent (which also waits for the remote policy)
        WebsCareAds.shared.checkConsent(viewController: topViewController) {
            WebsCareAds.shared.loadBanner(container: container, placement: placement)
        }
    }

    func loadNative(container: UIView, placement: String, size: String, onLoaded: @escaping (Shared.KotlinDouble) -> Void, onFailed: @escaping () -> Void) {
        pending[placement, default: []].append((container, onLoaded, onFailed))
        prepare(container, height: 132)
        // `size` is ignored by the iOS SDK for now (see defaultNativeSize in initialize)
        WebsCareAds.shared.checkConsent(viewController: topViewController) {
            WebsCareAds.shared.loadNative(container: container, placement: placement)
        }
    }

    /** The slot isn't on screen until its ad loads, so give the SDK a real size to lay the ad out in. */
    private func prepare(_ container: UIView, height: CGFloat) {
        if container.bounds.width == 0 {
            container.frame = CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: height)
        }
        // The SDK only fills a container that is in a window. Park it off-screen in the key window until
        // the Compose slot (UIKitView) takes it over once the ad has loaded.
        if container.superview == nil, let window = topViewController?.view.window {
            container.frame.origin = CGPoint(x: -10_000, y: 0)
            window.addSubview(container)
        }
    }

    /** One fill answers the oldest waiting slot of that placement. */
    private func slotLoaded(_ placement: String) {
        guard var queue = pending[placement], !queue.isEmpty else { return }
        let slot = queue.removeFirst()
        pending[placement] = queue.isEmpty ? nil : queue
        slot.loaded(Shared.KotlinDouble(double: Double(adHeight(in: slot.container))))
    }

    /** The height the SDK's ad view needs at the slot's width, so the Compose row matches the ad (no clipping). */
    private func adHeight(in container: UIView) -> CGFloat {
        container.layoutIfNeeded()
        let width = container.bounds.width > 0 ? container.bounds.width : UIScreen.main.bounds.width
        let fitted = container.subviews.map {
            $0.systemLayoutSizeFitting(
                CGSize(width: width, height: UIView.layoutFittingCompressedSize.height),
                withHorizontalFittingPriority: .required, verticalFittingPriority: .fittingSizeLevel
            ).height
        }.max() ?? 0
        let framed = container.subviews.map { $0.frame.maxY }.max() ?? 0
        let h = max(fitted, framed)
        return h > 1 ? h : container.bounds.height
    }

    /** A failure collapses every slot still waiting on that placement. */
    private func slotFailed(_ placement: String) {
        pending.removeValue(forKey: placement)?.forEach { $0.failed() }
    }

    private var topViewController: UIViewController? { GoogleSignInBridgeImpl.topViewController() }
}

#else

/** TEMP no-ads stand-in until the WebsCareAds-iOS-4.2.5 ZIP is in Vendor/. Every callback runs at once. */
final class AdsBridgeImpl: NSObject, AdsBridge {
    func initialize(isDebug: Bool) {}
    func setPremium(isPremium: Bool) {}
    func requestConsent(completion: @escaping (Shared.KotlinBoolean) -> Void) { completion(Shared.KotlinBoolean(bool: true)) }
    func isPrivacyOptionsRequired() -> Bool { false }
    func showPrivacyOptions(completion: @escaping () -> Void) { completion() }
    func preload(kind: String, placement: String) {}
    func showSplashAppOpen(placement: String, onContinue: @escaping () -> Void) { DispatchQueue.main.async { onContinue() } }
    func showInterstitial(placement: String, onDismissed: @escaping () -> Void) { DispatchQueue.main.async { onDismissed() } }
    func showRewarded(placement: String, onRewarded: @escaping (String, Shared.KotlinInt) -> Void, onNotReady: @escaping () -> Void) {
        // Grant the reward so "watch an ad to download" still downloads
        DispatchQueue.main.async { onRewarded("reward", Shared.KotlinInt(int: 1)) }
    }
    func setAppOpenLoadingScreenVisible(visible: Bool) {}
    func loadBanner(container: UIView, placement: String, onLoaded: @escaping (Shared.KotlinDouble) -> Void, onFailed: @escaping () -> Void) { DispatchQueue.main.async { onFailed() } }
    func loadNative(container: UIView, placement: String, size: String, onLoaded: @escaping (Shared.KotlinDouble) -> Void, onFailed: @escaping () -> Void) { DispatchQueue.main.async { onFailed() } }
}

#endif
