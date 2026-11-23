import UIKit
import Shared
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
    /** Native slots still waiting for an ad, by placement, so a failure can remove their rows. */
    private var pendingNative: [String: [() -> Void]] = [:]

    func initialize(isDebug: Bool) {
        WebsCareAds.shared.doInit { config in
            config.environment = isDebug ? AdEnvironment.test : AdEnvironment.production
            config.onAdFailed = { [weak self] adType, placement, _, message in
                if adType.lowercased().contains("native") {
                    DispatchQueue.main.async { self?.nativeFailed(placement) }
                }
                if isDebug { print("WebsCareAds onAdFailed: \(adType) '\(placement)' -> \(message)") }
            }
            config.onAdLoaded = { [weak self] adType, placement, _ in
                if adType.lowercased().contains("native") {
                    DispatchQueue.main.async { self?.pendingNative[placement] = nil }
                }
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

    func loadBanner(container: UIView, placement: String) {
        WebsCareAds.shared.loadBanner(container: container, placement: placement)
    }

    func loadNative(container: UIView, placement: String, onFailed: @escaping () -> Void) {
        pendingNative[placement, default: []].append(onFailed)
        WebsCareAds.shared.loadNative(container: container, placement: placement)
    }

    private func nativeFailed(_ placement: String) {
        pendingNative.removeValue(forKey: placement)?.forEach { $0() }
    }

    private var topViewController: UIViewController? { GoogleSignInBridgeImpl.topViewController() }
}
