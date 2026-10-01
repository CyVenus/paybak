import SwiftUI

/// Notifications & reminders (screens-settings §7): a switch per push type, the default reminder
/// schedule (independent checks for anything with a due date, loan installments included) and the
/// friends muted from their page. Every change is saved to the ledger settings, which the reminder
/// scheduler follows.
struct NotificationSettingsScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore

    private typealias Push = LedgerSettings.Push
    private typealias Schedule = LedgerSettings.ReminderSchedule

    private static let pushTypes: [(title: String, id: String, keyPath: WritableKeyPath<Push, Bool>)] = [
        ("Added to an expense", "addedToExpense", \.addedToExpense),
        ("Payments to confirm", "paymentsToConfirm", \.paymentsToConfirm),
        ("Reminders", "reminders", \.reminders),
        ("Overdue alerts", "overdueAlerts", \.overdueAlerts),
        ("Project updates", "projectUpdates", \.projectUpdates),
        ("Monthly summary", "monthlySummary", \.monthlySummary),
    ]

    private static let scheduleRules: [(title: String, id: String, keyPath: WritableKeyPath<Schedule, Bool>)] = [
        ("2 days before", "twoDaysBefore", \.twoDaysBefore),
        ("On the due date", "onDueDate", \.onDueDate),
        ("When overdue, every 3 days", "overdueEvery3Days", \.overdueEvery3Days),
    ]

    private var settings: LedgerSettings { ledgerStore.ledger.settings }

    var body: some View {
        SettingsScaffold(title: "Notifications", testIDPrefix: "settingsNotifications") {
            SettingsSection(title: "Push notifications") {
                VStack(spacing: 0) {
                    ForEach(Self.pushTypes, id: \.id) { type in
                        PBSettingRow(type.title, trailing: .toggle(pushBinding(type.keyPath)),
                                     showsDivider: type.id != Self.pushTypes.last?.id)
                            .accessibilityIdentifier("settingsNotifications.push.\(type.id)")
                    }
                }
                .pbCard(padding: 0)
            }
            SettingsSection(title: "Reminder schedule",
                            footer: "For anything with a due date. Friends who owe you get a gentle push.") {
                VStack(spacing: 0) {
                    ForEach(Self.scheduleRules, id: \.id) { rule in
                        let isOn = settings.reminderSchedule[keyPath: rule.keyPath]
                        PBSettingRow(rule.title, trailing: isOn ? .check : .unchecked,
                                     showsDivider: rule.id != Self.scheduleRules.last?.id) {
                            ledgerStore.updateSettings { $0.reminderSchedule[keyPath: rule.keyPath].toggle() }
                        }
                        .accessibilityIdentifier("settingsNotifications.schedule.\(rule.id)")
                    }
                }
                .pbCard(padding: 0)
            }
            SettingsSection(footer: "Muted friends don’t get automatic reminders.") {
                PBSettingRow("Muted friends", value: mutedValue, icon: .bell, showsDivider: false) {
                    router.open(.mutedFriends)
                }
                .pbCard(padding: 0)
                .accessibilityIdentifier("settingsNotifications.muted")
            }
        }
        .sensoryFeedback(.selection, trigger: settings.reminderSchedule)
    }

    /// "None", "1 friend", "{n} friends".
    private var mutedValue: String {
        let count = ledgerStore.ledger.people.filter(\.remindersMuted).count
        return switch count {
        case 0: "None"
        case 1: "1 friend"
        default: "\(count) friends"
        }
    }

    private func pushBinding(_ keyPath: WritableKeyPath<Push, Bool>) -> Binding<Bool> {
        Binding {
            settings.push[keyPath: keyPath]
        } set: { isOn in
            ledgerStore.updateSettings { $0.push[keyPath: keyPath] = isOn }
        }
    }
}

#Preview("NotificationSettingsScreen") {
    let profileStore = ProfileStore(defaults: UserDefaults(suiteName: "preview-notifications")!)
    NotificationSettingsScreen()
        .environment(AppRouter())
        .environment(LedgerStore(file: LedgerFile(url: .temporaryDirectory.appending(path: "preview-notifications.json")), profileStore: profileStore))
}
