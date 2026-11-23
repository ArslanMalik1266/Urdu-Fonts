import UIKit
import Shared
// import WebsCareAds   // TODO: once the WebsCare iOS pod is in the Podfile

/**
 * WebsCare Ads for iOS, for the shared Kotlin AdManager (IosAdManager). Placement names are the same as on
 * Android (urdufonts_home_native, urdufonts_download_interstitial, ...).
 *
 * Until the WebsCare iOS SDK is added, ads stay OFF: every "show" call continues at once (as WebsCare does on
 * no-fill), so the splash, downloads and rewards keep working. Replace each TODO with the matching WebsCare
 * call (the Android WebsCareAdManagerImpl.kt shows which one).
 */
final class AdsBridgeImpl: NSObject, AdsBridge {

    private var isPremium = false

    func initialize(isDebug: Bool) {
        // TODO: WebsCareAds.init(adapters: [AdMobAdapter()], isDebug: isDebug) { premiumCheck = { self.isPremium } }
        // TODO: WebsCareAds.enableAutoAppOpen(placement: "urdufonts_app_resume")
    }

    func setPremium(isPremium: Bool) {
        self.isPremium = isPremium
        // TODO: WebsCareAds.setPremium(isPremium)
    }

    func requestConsent(completion: @escaping (KotlinBoolean) -> Void) {
        // TODO: WebsCareAds.requestConsent(from: topViewController) { gathered in completion(KotlinBoolean(bool: gathered)) }
        completion(KotlinBoolean(bool: true))
    }

    func isPrivacyOptionsRequired() -> Bool {
        // TODO: WebsCareAds.isPrivacyOptionsRequired()
        false
    }

    func showPrivacyOptions(completion: @escaping () -> Void) {
        // TODO: WebsCareAds.showConsentForm(from: topViewController, completion: completion)
        completion()
    }

    func preload(kind: String, placement: String) {
        guard !isPremium else { return }
        // TODO: switch kind { case "app_open": WebsCareAds.preloadAppOpen(placement)
        //                     case "interstitial": WebsCareAds.preloadInterstitial(placement)
        //                     case "rewarded": WebsCareAds.preloadRewarded(placement) }
    }

    func showSplashAppOpen(placement: String, onContinue: @escaping () -> Void) {
        // TODO: WebsCareAds.checkConsent { WebsCareAds.loadAndShowAppOpen(placement, onDismissed: onContinue) }
        onContinue()
    }

    func showInterstitial(placement: String, onDismissed: @escaping () -> Void) {
        // TODO: WebsCareAds.showInterstitial(from: topViewController, placement: placement, onDismissed: onDismissed)
        onDismissed()
    }

    func showRewarded(placement: String, onRewarded: @escaping (String, KotlinInt) -> Void, onNotReady: @escaping () -> Void) {
        // TODO: WebsCareAds.showRewarded(from: topViewController, placement: placement,
        //           onRewarded: { type, amount in onRewarded(type, KotlinInt(int: Int32(amount))) }, onNotReady: onNotReady)
        onNotReady()
    }

    func suppressNextReturnAppOpen() {
        // TODO: WebsCareAds.suppressNextReturnAppOpen()
    }

    func setAppOpenLoadingScreenVisible(visible: Bool) {
        // TODO: WebsCareAds.setAppOpenLoadingScreenVisible(visible)
    }

    func makeBannerView(placement: String) -> UIView? {
        guard !isPremium else { return nil }
        // TODO: return WebsCareAds.bannerView(placement: placement, rootViewController: topViewController)
        return nil
    }

    func makeNativeView(placement: String, small: Bool, onFailed: @escaping () -> Void) -> UIView? {
        guard !isPremium else { return nil }
        // TODO: return WebsCareAds.nativeView(placement: placement, size: small ? .small : .medium, onFailed: onFailed)
        onFailed()
        return nil
    }

    private var topViewController: UIViewController? { GoogleSignInBridgeImpl.topViewController() }
}
