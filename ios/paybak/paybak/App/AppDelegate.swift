import UIKit
import UserNotifications

/// Receives local-notification taps and actions (app-architecture §2.6, §4): "Confirm" confirms the
/// claim without opening the app, "Not received" and taps open the notification's link.
final class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    private weak var ledgerStore: LedgerStore?
    private weak var router: AppRouter?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        let center = UNUserNotificationCenter.current()
        center.delegate = self
        center.setNotificationCategories(NotificationService.categories)
        return true
    }

    /// Gives the delegate the stores it acts on once the scene is up.
    func connect(ledgerStore: LedgerStore, router: AppRouter) {
        self.ledgerStore = ledgerStore
        self.router = router
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification) async -> UNNotificationPresentationOptions {
        [.banner, .list, .sound]
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse) async {
        let userInfo = response.notification.request.content.userInfo
        let link = (userInfo[NotificationService.linkKey] as? String).flatMap(DeepLink.init)
        switch response.actionIdentifier {
        case NotificationService.confirmAction:
            if case .claim(let id, _) = link {
                try? ledgerStore?.confirmPayment(id)
            }
        case NotificationService.notReceivedAction:
            if case .claim(let id, _) = link {
                open(.claim(id, notReceived: true))
            }
        case UNNotificationDefaultActionIdentifier:
            if let link { open(link) }
        default:
            break
        }
    }

    private func open(_ link: DeepLink) {
        guard let router else { return }
        if router.root == .main {
            router.open(link)
        } else {
            router.pendingLink = link
        }
    }
}
