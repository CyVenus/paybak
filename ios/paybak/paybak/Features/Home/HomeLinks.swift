import Foundation

// Where every Home row and button leads (app-architecture §2.3, home-v2 §2.2).

extension DueSoonRow {
    /// A share you owe inside a group: the row shows the group, not the person.
    var isGroupShare: Bool {
        obligation.debtor == Person.me && obligation.kind == .group
    }

    /// The row body: the group for a group share, otherwise the friend's page.
    var route: Route {
        isGroupShare ? .group(obligation.ref) : .friend(obligation.friend)
    }

    /// Remind opens the reminder sheet about this item; Settle opens Record payment prefilled from
    /// the settle plan: you pay the (simplified) payee the amount, by UPI when they have an ID.
    func actionRoute(in books: Books) -> Route {
        switch action {
        case .remind:
            return .remind(personId: obligation.friend, context: obligation.reminderContext)
        case .settle:
            let payee = obligation.creditor
            return .recordPayment(RecordPaymentArgs(
                from: Person.me,
                to: payee,
                amount: amount,
                currency: books.defaultCurrency,
                method: books.ledger.person(payee)?.upi == nil ? nil : .upi,
                context: obligation.paymentContext
            ))
        }
    }
}

extension Obligation {
    /// What a reminder about this item refers to.
    var reminderContext: ReminderContext {
        switch kind {
        case .direct: .expense(ref)
        case .group, .project: .group(ref)
        case .loan: .loan(ref)
        }
    }

    /// What a payment settling this item is for.
    var paymentContext: PaymentContext {
        switch kind {
        case .direct: .expense(ref)
        case .group, .project: .group(ref)
        case .loan: .loan(ref)
        }
    }
}

extension RecentActivityRow {
    /// The expense or payment detail.
    var route: Route {
        switch kind {
        case .expense(let id, _): .expense(id)
        case .payment(let id, _): .payment(id)
        }
    }

    /// The record the row is about (its test id suffix).
    var recordID: String {
        switch kind {
        case .expense(let id, _): id
        case .payment(let id, _): id
        }
    }
}

extension HomeSummary {
    /// The designed Home the data calls for (the `screen.<id>` root, app-architecture §1.2): any
    /// pending claim shows Confirm payment on top of the Active layout.
    var screen: ScreenID {
        if !pendingClaims.isEmpty { return .homeConfirmPayment }
        return switch state {
        case .firstDay: .homeFirstDay
        case .active: .homeActive
        case .allSettled: .homeAllSettled
        }
    }

    /// `home.state.<id>` for UI tests.
    var stateID: String {
        pendingClaims.isEmpty ? state.rawValue : "confirmPayment"
    }
}
