import Foundation

/// An internal link carried by a notification (`userInfo["link"]`) or the debug `-link` argument
/// (app-architecture §2.6). No URL scheme is registered; links only come from inside the app.
enum DeepLink: Hashable {
    /// The Activity timeline with a claim on top; `notReceived` also opens its sheet.
    case claim(PaymentID, notReceived: Bool)
    case recordPayment(to: PersonID, amount: Int64?, context: PaymentContext?, method: PaymentMethodKind? = nil)
    case insights(YearMonth?)
    case remind(PersonID)
    case expense(ExpenseID)
    case recurringDraft(DraftID)
    case payment(PaymentID)

    init?(_ text: String) {
        guard let url = URL(string: text), url.scheme == "paybak", let host = url.host() else { return nil }
        let query = Dictionary(
            (URLComponents(url: url, resolvingAgainstBaseURL: false)?.queryItems ?? []).map { ($0.name, $0.value ?? "") },
            uniquingKeysWith: { first, _ in first }
        )
        let tail = url.pathComponents.dropFirst().first
        switch host {
        case "activity":
            guard let claim = query["claim"] else { return nil }
            self = .claim(claim, notReceived: query["action"] == "notReceived")
        case "record-payment":
            guard let to = query["to"] else { return nil }
            self = .recordPayment(to: to, amount: query["amount"].flatMap { Int64($0) }, context: query["context"].flatMap { Self.context($0) },
                                  method: query["method"].flatMap(PaymentMethodKind.init(rawValue:)))
        case "insights":
            let parts = query["month"]?.split(separator: "-").compactMap { Int($0) } ?? []
            self = .insights(parts.count == 2 ? YearMonth(year: parts[0], month: parts[1]) : nil)
        case "remind":
            guard let person = query["person"] else { return nil }
            self = .remind(person)
        case "expense":
            guard let id = tail else { return nil }
            self = .expense(id)
        case "recurring-draft":
            guard let id = tail else { return nil }
            self = .recurringDraft(id)
        case "payment":
            guard let id = tail else { return nil }
            self = .payment(id)
        default:
            return nil
        }
    }

    /// The link text (what a notification carries).
    var text: String {
        switch self {
        case .claim(let id, let notReceived): "paybak://activity?claim=\(id)" + (notReceived ? "&action=notReceived" : "")
        case .recordPayment(let to, let amount, let context, let method):
            "paybak://record-payment?to=\(to)" + (amount.map { "&amount=\($0)" } ?? "") + (context.map { "&context=\(Self.text($0))" } ?? "")
                + (method.map { "&method=\($0.rawValue)" } ?? "")
        case .insights(let month): "paybak://insights" + (month.map { "?month=\($0.key)" } ?? "")
        case .remind(let person): "paybak://remind?person=\(person)"
        case .expense(let id): "paybak://expense/\(id)"
        case .recurringDraft(let id): "paybak://recurring-draft/\(id)"
        case .payment(let id): "paybak://payment/\(id)"
        }
    }

    private static func context(_ text: String) -> PaymentContext? {
        let parts = text.split(separator: ":", maxSplits: 1).map(String.init)
        guard parts.count == 2 else { return nil }
        switch parts[0] {
        case "group": return .group(parts[1])
        case "expense": return .expense(parts[1])
        case "loan": return .loan(parts[1])
        default: return nil
        }
    }

    private static func text(_ context: PaymentContext) -> String {
        switch context {
        case .group(let id): "group:\(id)"
        case .expense(let id): "expense:\(id)"
        case .loan(let id): "loan:\(id)"
        }
    }
}

extension AppRouter {
    /// Opens a deep link exactly as a notification tap would. Ignored while onboarding.
    func open(_ link: DeepLink) {
        guard root == .main else { return }
        switch link {
        case .claim(let id, let notReceived):
            select(.activity)
            activitySegment = .timeline
            if notReceived { open(Route.notReceived(id)) }
        case .recordPayment(let to, let amount, let context, let method):
            select(.home)
            open(Route.recordPayment(RecordPaymentArgs(from: Person.me, to: to, amount: amount, method: method, context: context)))
        case .insights(let month):
            select(.activity)
            activitySegment = .insights
            insightsMonth = month
        case .remind(let person):
            select(.home)
            open(Route.remind(personId: person, context: nil))
        case .expense(let id):
            select(.activity)
            open(Route.expense(id))
        case .recurringDraft(let id):
            open(Route.enterDraftAmount(id))
        case .payment(let id):
            select(.activity)
            open(Route.payment(id))
        }
    }
}
