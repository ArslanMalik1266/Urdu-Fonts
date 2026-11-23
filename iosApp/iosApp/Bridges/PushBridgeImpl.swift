import Foundation
import FirebaseMessaging
import Shared

/** The FCM token for the shared Kotlin push registration (same backend endpoint as Android, platform "ios"). */
final class PushBridgeImpl: NSObject, PushBridge {
    func currentToken(completion: @escaping (String?) -> Void) {
        // Firebase needs the APNs token first; without it there is no FCM token yet (it is retried on the next start)
        guard Messaging.messaging().apnsToken != nil else {
            completion(nil)
            return
        }
        Messaging.messaging().token { token, _ in completion(token) }
    }
}
