import SwiftUI
import Shared
import FirebaseCore
import FirebaseMessaging
import GoogleSignIn
import UserNotifications

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    @Environment(\.scenePhase) private var scenePhase

    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea(.all)
                // Google Sign-In returns to the app through its URL scheme
                .onOpenURL { url in GIDSignIn.sharedInstance.handle(url) }
        }
        .onChange(of: scenePhase) { phase in
            if phase == .active { IosAppKt.onAppResumed() }
        }
    }
}

final class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        FirebaseApp.configure()
        Messaging.messaging().delegate = self
        UNUserNotificationCenter.current().delegate = self

        let info = Bundle.main.infoDictionary ?? [:]
        let version = info["CFBundleShortVersionString"] as? String ?? ""
        #if DEBUG
        let isDebug = true
        #else
        let isDebug = false
        #endif

        // The shared Kotlin app: same network, data, screens as Android, with these iOS SDK bridges
        IosAppKt.doInitUrduFonts(
            bridges: IosBridges(
                storeKit: StoreKitBridgeImpl(),
                googleSignIn: GoogleSignInBridgeImpl(),
                push: PushBridgeImpl(),
                ads: AdsBridgeImpl()
            ),
            apiKey: info["UFApiKey"] as? String ?? "",
            iapAppToken: info["UFIapAppToken"] as? String ?? "",
            versionName: version,
            isDebug: isDebug
        )

        // A launch from a tapped notification while the app was closed
        if let userInfo = launchOptions?[.remoteNotification] as? [AnyHashable: Any] {
            IosAppKt.onNotificationTapped(userInfo: userInfo)
        }
        // Get an APNs token now if notifications are already allowed (asking happens in the app's settings flow)
        UNUserNotificationCenter.current().getNotificationSettings { settings in
            if settings.authorizationStatus == .authorized || settings.authorizationStatus == .provisional {
                DispatchQueue.main.async { application.registerForRemoteNotifications() }
            }
        }
        return true
    }

    // APNs → Firebase, so FCM can deliver the backend's pushes
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }

    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        IosAppKt.onPushTokenRefreshed()
    }

    // Show pushes while the app is open too
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound, .list])
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        IosAppKt.onNotificationTapped(userInfo: response.notification.request.content.userInfo)
        completionHandler()
    }
}
