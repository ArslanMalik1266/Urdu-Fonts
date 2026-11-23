import UIKit
import GoogleSignIn
import Shared

/**
 * Google's account picker for the shared Kotlin sign-in (IosGoogleSignIn). The ID token is sent to the IAP
 * backend (auth/oauth/login) exactly like on Android. The iOS client id comes from GIDClientID in Info.plist;
 * the backend must also accept tokens issued to that iOS client id.
 */
final class GoogleSignInBridgeImpl: NSObject, GoogleSignInBridge {

    // The same web (server) client id Android uses, so the ID token's audience is what the backend expects
    private let serverClientId = "403532354068-5io7gljou5nfqpkd5kcs0o7f3gv3bmc1.apps.googleusercontent.com"

    func signIn(completion: @escaping (GoogleAccount?, String?) -> Void) {
        DispatchQueue.main.async {
            guard let clientId = Bundle.main.object(forInfoDictionaryKey: "GIDClientID") as? String,
                  !clientId.hasPrefix("REPLACE_ME") else {
                completion(nil, "Google sign-in is not set up for iOS yet")
                return
            }
            guard let presenter = Self.topViewController() else {
                completion(nil, "No screen to show Google sign-in on")
                return
            }
            GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientId, serverClientID: self.serverClientId)
            GIDSignIn.sharedInstance.signIn(withPresenting: presenter) { result, error in
                guard let user = result?.user, let idToken = user.idToken?.tokenString else {
                    completion(nil, error?.localizedDescription ?? "Google sign-in was cancelled")
                    return
                }
                completion(
                    GoogleAccount(
                        idToken: idToken,
                        email: user.profile?.email ?? "",
                        name: user.profile?.name,
                        photoUrl: user.profile?.imageURL(withDimension: 96)?.absoluteString
                    ),
                    nil
                )
            }
        }
    }

    static func topViewController() -> UIViewController? {
        let root = UIApplication.shared.connectedScenes
            .compactMap { ($0 as? UIWindowScene)?.keyWindow }
            .first?.rootViewController
        var top = root
        while let presented = top?.presentedViewController { top = presented }
        return top
    }
}
