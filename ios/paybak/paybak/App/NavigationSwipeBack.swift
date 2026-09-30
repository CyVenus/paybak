import SwiftUI
import UIKit

extension View {
    /// Hides the system navigation bar (onboarding screens draw `PBOnboardingTopBar` or
    /// `PBSetupHeader` instead) but keeps the iOS edge-swipe back gesture, which UIKit turns off
    /// along with a hidden bar. A screen that also applies `navigationBarBackButtonHidden()` (All set)
    /// can't be swiped back, as with the system bar.
    func navigationBarHiddenKeepingSwipeBack() -> some View {
        toolbar(.hidden, for: .navigationBar)
            .background { SwipeBackEnabler() }
    }
}

/// Becomes the delegate of the enclosing navigation controller's `interactivePopGestureRecognizer`
/// whenever its screen appears. UIKit's own delegate refuses the gesture while the bar is hidden;
/// this one allows it whenever there's a screen to go back to and the top screen shows back.
private struct SwipeBackEnabler: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> Controller {
        Controller()
    }

    func updateUIViewController(_ controller: Controller, context: Context) {}

    final class Controller: UIViewController, UIGestureRecognizerDelegate {
        override func loadView() {
            view = UIView()
            view.isUserInteractionEnabled = false
        }

        override func viewWillAppear(_ animated: Bool) {
            super.viewWillAppear(animated)
            navigationController?.interactivePopGestureRecognizer?.delegate = self
        }

        func gestureRecognizerShouldBegin(_ gestureRecognizer: UIGestureRecognizer) -> Bool {
            guard let navigationController,
                  navigationController.viewControllers.count > 1,
                  navigationController.transitionCoordinator == nil,
                  let topScreen = navigationController.topViewController
            else { return false }
            return !topScreen.navigationItem.hidesBackButton
        }
    }
}
