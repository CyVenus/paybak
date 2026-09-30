#if DEBUG
import Foundation

/// The M2 sections of the debug menu (app-architecture §3.10): data, clock, Pro and the friend's side.
enum CoreDebugActions {
    static func sections(_ ledger: Ledger) -> [DebugSection] {
        [data, clock, pro(ledger), friendsSide(ledger)]
    }

    static let data = DebugSection(title: "Data", actions: [
        DebugAction(title: "Load demo data at the Figma date", detail: "Wed 30 Sep 2026, 21:15 · Esha’s claim pending") { context in
            try context.ledgerStore.loadDemo(scenarios: Scenario.demo)
            context.router.select(.home)
        },
        DebugAction(title: "Load demo data around today", detail: "The same story on today’s dates, real clock") { context in
            try context.ledgerStore.loadDemo(atFigmaDate: false, scenarios: Scenario.demo)
            context.router.select(.home)
        },
        DebugAction(title: "Start an empty account", detail: "The demo profile with no records") { context in
            try context.ledgerStore.loadDemo(scenarios: Scenario.empty)
            context.router.select(.home)
        },
        DebugAction(title: "Reset onboarding", detail: "Clears the profile and the ledger") { context in
            DebugStart.resetEverything(profileStore: context.profileStore, ledgerStore: context.ledgerStore)
            context.router.restartOnboarding()
        },
    ])

    static let clock = DebugSection(title: "Clock", actions: [
        DebugAction(title: "Pin to 30 Sep 2026 21:15") { context in
            context.ledgerStore.setClock(DemoSeed.figmaNow(calendar: context.ledgerStore.clock.calendar))
        },
        DebugAction(title: "Use real time") { context in
            context.ledgerStore.setClock(nil)
        },
        DebugAction(title: "+1 day", detail: "Runs the scheduler up to the new time") { context in
            let clock = context.ledgerStore.clock
            context.ledgerStore.setClock(clock.calendar.date(byAdding: .day, value: 1, to: clock.now))
        },
        DebugAction(title: "Run tick now") { context in
            context.ledgerStore.tick()
        },
    ])

    static func pro(_ ledger: Ledger) -> DebugSection {
        let entitlement = ledger.settings.entitlement
        return DebugSection(title: "Pro", actions: [
            DebugAction(title: "Toggle Pro", detail: "Now: \(entitlement.isPro ? "Pro" : "Free")") { context in
                context.ledgerStore.setPro(!entitlement.isPro)
            },
            DebugAction(title: "Clear the entitlement") { context in
                context.ledgerStore.updateSettings { $0.entitlement = Entitlement() }
            },
        ])
    }

    /// Each item runs the store action a real sync would, "as" the friend.
    static func friendsSide(_ ledger: Ledger) -> DebugSection {
        let pending = ledger.payments.last { $0.fromId == Person.me && $0.status == .pending }
        let payee = pending.flatMap { ledger.person($0.toId)?.firstName } ?? "The payee"
        var actions = [
            DebugAction(title: "Esha says she paid ₹700", detail: "A claim for Dinner at Olive Garden") { context in
                let store = context.ledgerStore
                let id = store.ledger.payment("pay-esha-olive") == nil ? "pay-esha-olive" : RecordID.make()
                try store.recordPayment(PaymentDraft(id: id, fromId: "p-esha", toId: Person.me, amount: 70_000, currency: "INR",
                                                     date: store.clock.today, expenseId: "e-olive", recordedBy: "p-esha"))
                if let claim = store.snapshot.home.pendingClaims.first(where: { $0.id == id }) {
                    Task { await NotificationService.postClaim(claim) }
                }
            },
        ]
        if let pending {
            actions += [
                DebugAction(title: "\(payee) confirms my latest pending payment") { context in
                    try context.ledgerStore.confirmPayment(pending.id)
                },
                DebugAction(title: "\(payee) says Not received") { context in
                    try context.ledgerStore.markNotReceived(pending.id, note: "Hi, I haven’t received it yet. Could you check?")
                },
            ]
        }
        actions += [
            DebugAction(title: "Esha flags Seafood dinner") { context in
                try context.ledgerStore.flagExpense("e-goa-seafood", by: "p-esha", note: "I left before dessert. Can we check the bill?")
            },
            DebugAction(title: "Esha removes her flag") { context in
                try context.ledgerStore.resolveFlag("e-goa-seafood", by: "p-esha")
            },
            DebugAction(title: "Priya comments on Villa") { context in
                try context.ledgerStore.addComment(to: "e-goa-villa", text: "Looks right to me.", by: "p-priya")
            },
            DebugAction(title: "Meera adds an expense in Flat 302", detail: "Groceries ₹900, split three ways") { context in
                let store = context.ledgerStore
                let members = [Person.me, "p-meera", "p-kabir"]
                try store.addExpense(ExpenseDraft(groupId: "g-flat302", title: "Groceries", category: .food, amount: 90_000, currency: "INR",
                                                  date: store.clock.today, payers: [Payer(personId: "p-meera", amount: 90_000)],
                                                  rows: members.map { SplitRow(personId: $0) }), by: "p-meera")
            },
        ]
        return DebugSection(title: "Friend’s side", actions: actions)
    }
}
#endif
