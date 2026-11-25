import UIKit
import AuthenticationServices
import Shared

/**
 * Apple's sign-in sheet for the shared Kotlin sign-in (IosAppleSignIn). The identity token is sent to the IAP
 * backend (auth/oauth/login, provider "apple"), which verifies it with Apple; its audience is the bundle id.
 */
final class AppleSignInBridgeImpl: NSObject, AppleSignInBridge,
    ASAuthorizationControllerDelegate, ASAuthorizationControllerPresentationContextProviding {

    private var completion: ((AppleAccount?, String?) -> Void)?

    func signInWithApple(completion: @escaping (AppleAccount?, String?) -> Void) {
        DispatchQueue.main.async {
            self.completion = completion
            let request = ASAuthorizationAppleIDProvider().createRequest()
            request.requestedScopes = [.fullName, .email]
            let controller = ASAuthorizationController(authorizationRequests: [request])
            controller.delegate = self
            controller.presentationContextProvider = self
            controller.performRequests()
        }
    }

    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        UIApplication.shared.connectedScenes
            .compactMap { ($0 as? UIWindowScene)?.keyWindow }
            .first ?? ASPresentationAnchor()
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization authorization: ASAuthorization) {
        guard let credential = authorization.credential as? ASAuthorizationAppleIDCredential,
              let tokenData = credential.identityToken,
              let idToken = String(data: tokenData, encoding: .utf8) else {
            finish(nil, "Apple did not return an identity token")
            return
        }
        let name = [credential.fullName?.givenName, credential.fullName?.familyName]
            .compactMap { $0 }
            .joined(separator: " ")
        finish(AppleAccount(idToken: idToken, email: credential.email, name: name.isEmpty ? nil : name), nil)
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        if let authError = error as? ASAuthorizationError, authError.code == .canceled {
            finish(nil, "Sign in with Apple was cancelled")
        } else {
            finish(nil, error.localizedDescription)
        }
    }

    private func finish(_ account: AppleAccount?, _ error: String?) {
        let done = completion
        completion = nil
        done?(account, error)
    }
}
