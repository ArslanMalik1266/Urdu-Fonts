# Urdu Fonts – iOS app

The iOS app is a thin Swift shell around the shared Kotlin Multiplatform module (`../shared`): every screen,
the networking, the database and the business logic are the same code the Android app runs. Swift only provides
the Apple / third-party SDKs that Kotlin cannot call through small bridges (`iosApp/Bridges/`: StoreKit 2, Google Sign-In,
Firebase Messaging) plus one for the WebsCareAds XCFramework.

## First build (on a Mac)

Requirements: Xcode 26.2+ (required by WebsCareAds 4.2.5), JDK 21, [XcodeGen](https://github.com/yonaskolb/XcodeGen)
and CocoaPods (`brew install xcodegen cocoapods`). Gradle on the Mac needs the same `gpr.key` (GitHub Packages token) in
`gradle.properties` as on Windows, to download WebsCareAds.

```sh
cd iosApp
cp Configuration/Secrets.xcconfig.example Configuration/Secrets.xcconfig   # fill in API_KEY / IAP token
# add GoogleService-Info.plist (Firebase iOS app for the same bundle id) to iosApp/iosApp/
# put the WebsCareAds 4.2.5 release ZIP contents in Vendor/WebsCareAds-iOS-4.2.5/ (see "Ads" below)
xcodegen generate
pod install
open iosApp.xcworkspace
```

Pick the `iosApp` scheme, a simulator or your iPhone, set your team under Signing & Capabilities, and Run. The
first build compiles the Kotlin code (`./gradlew :shared:embedAndSignAppleFrameworkForXcode`), which takes a few
minutes.

> The Kotlin `iosMain` code has not been compiled yet (iOS can only be built on macOS), so the first build may
> show a few Kotlin compile errors to fix.

## What still needs your input

| Item | Where |
|---|---|
| iOS app in the WebsCare ads policy | The owner registers bundle `com.urdufonts.app` (iOS) in the WebsCare Firebase project (parameter `com_urdufonts_app_ios`) with the same placement names as Android: `urdufonts_app_open`, `urdufonts_app_resume`, `urdufonts_home_native`, `urdufonts_font_list_native`, `urdufonts_categories_native`, `urdufonts_styles_native`, `urdufonts_font_details_banner`, `urdufonts_download_interstitial`, `urdufonts_download_font_rewarded`. Until then the SDK uses Google demo inventory. |
| iOS AdMob application id | `Configuration/Config.xcconfig` → `ADMOB_APP_ID` (Google's demo id is set now) |
| SKAdNetwork list | `project.yml` → `SKAdNetworkItems`: paste Google's (and Meta's) current list |
| Google Sign-In iOS client id | `Configuration/Config.xcconfig` → `GOOGLE_IOS_CLIENT_ID` / `GOOGLE_REVERSED_CLIENT_ID`; the IAP backend must accept tokens for it |
| App Store products | Create them in App Store Connect (one subscription group: Basic < Pro < Business, plus the lifetime non-consumable) and add the same product ids to the backend's **iOS** catalog. Ids are in `shared/src/iosMain/.../ios/IosBillingRepository.kt` → `IosProductIds` |
| App Store ids for links | `shared/src/iosMain/.../ui/platform/PlatformUi.ios.kt` → `IosStoreIds` (Rate us, Urdu Canvas) |
| Push | upload the APNs key to the app's Firebase project; set `aps-environment` to `production` for release |

## Purchases (StoreKit 2 → IAP backend)

Follows the backend's iOS integration guide: the purchase carries the member's `app_account_token` from
`GET me/purchase-identity` (checkout is refused without one), the verified transaction is sent to
`POST /api/v1/purchases/ios/confirm` as `signed_transaction_info` (+ `signed_renewal_info`, `transaction_id`,
`sku` = App Store product id, `product_type`), and the transaction is finished only after the backend accepts
it. A failed confirmation stays unfinished, so StoreKit delivers it again. Premium comes from the backend's
entitlements, never from StoreKit alone.

## Ads (WebsCareAds 4.2.5)

The iOS SDK is the WebsCareAds XCFramework, installed with its own podspec (the route in
`IOS_DEVELOPER_HANDOFF.md`). Put the **release ZIP** (not the source repository) at
`iosApp/Vendor/WebsCareAds-iOS-4.2.5/`, so that folder holds `WebsCareAds.podspec`, `WebsCareAds.xcframework` and
`WebsCareAdsPrivacy.bundle`: download it from https://github.com/webscarepk/WebCareAds/releases/tag/v4.2.5, or
build it on the Mac from the SDK source with `./gradlew :webscare-ads:assembleWebsCareAdsReleaseXCFramework` and copy
`ios/WebsCareAds.podspec` + `ios/Resources/WebsCareAdsPrivacy.bundle` next to it. `pod install` then brings Google Mobile
Ads 13.11.0, UMP 3.1.0, the Meta adapter, Firebase 12.19.0 and the `-ObjC` flag.

`Bridges/AdsBridgeImpl.swift` calls it for the shared Kotlin `IosAdManager`: debug builds use `AdEnvironment.test`,
release asks for `production` (still gated by the remote policy's `test_mode`), consent runs before any ad, premium
users get no ads, and the splash shows the app-open ad through `loadAndShowAppOpen` exactly as on Android.
(WebsCareAds is not published for iOS on Maven, so Kotlin cannot use it directly; `Shared` is a dynamic framework so
its Kotlin runtime stays separate from WebsCareAds' own.) Follow `IOS_DEVELOPER_HANDOFF.md` for Meta privacy signals
and the acceptance checks; never click live ads.
## How the pieces fit

| Feature | Android (`app` + `shared/androidMain`) | iOS (`iosApp` + `shared/iosMain`) |
|---|---|---|
| Subscriptions | Play Billing (`BillingRepositoryImpl`) | StoreKit 2 (`StoreKitBridgeImpl.swift` → `IosBillingRepository`) |
| Ads | WebsCareAds (Android) | WebsCareAds XCFramework (`AdsBridgeImpl.swift` → `IosAdManager`) |
| Google sign-in | Credential Manager | GoogleSignIn SDK |
| Push | FCM | FCM on APNs |
| Saving fonts | Downloads/UrduFonts (MediaStore) | Documents/UrduFonts (Files app) |
| Everything else | shared `commonMain` | shared `commonMain` |
