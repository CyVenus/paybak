import UserNotifications

/// The OS notification permission (Setup 4 "Turn on notifications").
enum NotificationPermission {
    /// Shows the system prompt if the user hasn't answered it yet; otherwise returns the earlier
    /// answer without prompting.
    static func request() async -> UserProfile.NotificationsChoice {
        let center = UNUserNotificationCenter.current()
        switch await center.notificationSettings().authorizationStatus {
        case .notDetermined:
            let granted = (try? await center.requestAuthorization(options: [.alert, .badge, .sound])) ?? false
            return granted ? .allowed : .denied
        case .denied:
            return .denied
        default:
            return .allowed
        }
    }
}
