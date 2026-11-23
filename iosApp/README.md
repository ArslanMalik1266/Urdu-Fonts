# Urdu Fonts – iOS app

The iOS app is a thin Swift shell around the shared Kotlin Multiplatform module (`../shared`): every screen,
the networking, the database and the business logic are the same code the Android app runs. Swift only provides
the Apple / third-party SDKs through four small bridges (`iosApp/Bridges/`).

## First build (on a Mac)

Requirements: Xcode 16+, JDK 17+, [XcodeGen](https://github.com/yonaskolb/XcodeGen) and CocoaPods
(`brew install xcodegen cocoapods`).

```sh
cd iosApp
cp Configuration/Secrets.xcconfig.example Configuration/Secrets.xcconfig   # fill in API_KEY / IAP token
# add GoogleService-Info.plist (Firebase iOS app for the same bundle id) to iosApp/iosApp/
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
| WebsCare Ads iOS pod | `Podfile` (uncomment / fix the pod line) and the TODOs in `Bridges/AdsBridgeImpl.swift` |
| Google Sign-In iOS client id | `Configuration/Config.xcconfig` → `GOOGLE_IOS_CLIENT_ID` and `GOOGLE_REVERSED_CLIENT_ID` |
| AdMob iOS app id | `Configuration/Config.xcconfig` → `ADMOB_APP_ID` (a Google test id is set now) |
| App Store product ids | `shared/src/iosMain/.../ios/IosBillingRepository.kt` → `IosProductIds` (defaults: monthly, monthly-pro, monthly-business, urdu_fonts_lifetime) |
| iOS purchase endpoint | the app posts to `api/v1/purchases/ios/confirm` with the StoreKit 2 signed transaction (JWS) in `purchase_token`; confirm the path and fields with the backend |
| App Store ids for links | `shared/src/iosMain/.../ui/platform/PlatformUi.ios.kt` → `IosStoreIds` (Rate us, Urdu Canvas) |
| Push | upload the APNs key to Firebase; switch `aps-environment` to `production` for release |

## How the pieces fit

| Feature | Android (`app` + `shared/androidMain`) | iOS (`iosApp` + `shared/iosMain`) |
|---|---|---|
| Subscriptions | Play Billing (`BillingRepositoryImpl`) | StoreKit 2 (`StoreKitBridgeImpl.swift` → `IosBillingRepository`) |
| Ads | WebsCare Ads Android | WebsCare Ads iOS (`AdsBridgeImpl.swift` → `IosAdManager`) |
| Google sign-in | Credential Manager | GoogleSignIn SDK |
| Push | FCM | FCM on APNs |
| Saving fonts | Downloads/UrduFonts (MediaStore) | Documents/UrduFonts (Files app) |
| Everything else | shared `commonMain` | shared `commonMain` |
