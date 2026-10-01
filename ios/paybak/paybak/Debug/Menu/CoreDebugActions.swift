#if DEBUG
import Foundation

/// M2's sections of the debug menu (app-architecture §3.10): data, clock, Pro, the friend's side,
/// every seed scenario and every start screen.
enum CoreDebugActions {
    private static let esha: PersonID = "p-esha"
    private static let priya: PersonID = "p-priya"
    private static let meera: PersonID = "p-meera"
    private static let olive: ExpenseID = "e-olive"
    private static let seafood: ExpenseID = "e-goa-seafood"
    private static let villa: ExpenseID = "e-goa-villa"
    private static let flat302: GroupID = "g-flat302"

    static func sections(_ ledger: Ledger) -> [DebugSection] {
        [data, clock, pro(ledger), friendsSide(ledger), scenarios, loadScreen]
    }

    static let data = DebugSection(title: "Data", actions: [
        DebugAction(title: "Load demo data at the Figma date", detail: "Wed 30 Sep 2026, 9:15 pm, with Esha’s claim") { context in
            try context.ledgerStore.loadDemo(scenarios: Scenario.demo)
            context.router.resetMain()
        },
        DebugAction(title: "Load demo data around today", detail: "The same story on today’s dates, real clock") { context in
            try context.ledgerStore.loadDemo(atFigmaDate: false, scenarios: Scenario.demo)
            context.router.resetMain()
        },
        DebugAction(title: "Start an empty account", detail: "Keeps the profile") { context in
            context.ledgerStore.clear()
            context.router.resetMain()
        },
        DebugAction(title: "Reset onboarding", detail: "Clears the profile and the ledger") { context in
            DebugStart.resetEverything(profileStore: context.profileStore, ledgerStore: context.ledgerStore)
            // As a fresh launch: Splash, then Welcome step 1.
            context.router.restartOnboarding()
            context.router.showOnboarding(.splash)
        },
    ])

    static let clock = DebugSection(title: "Clock", actions: [
        DebugAction(title: "Pin to 30 Sep 2026 21:15") { context in
            context.ledgerStore.setClock(DemoSeed.figmaNow(calendar: context.ledgerStore.clock.calendar))
        },
        DebugAction(title: "Use real time") { context in
            context.ledgerStore.setClock(nil)
        },
        DebugAction(title: "+1 day") { context in
            context.ledgerStore.setClock(context.ledgerStore.clock.now.addingTimeInterval(24 * 60 * 60))
        },
        DebugAction(title: "Run tick now") { context in
            context.ledgerStore.tick()
        },
    ])

    static func pro(_ ledger: Ledger) -> DebugSection {
        let isPro = ledger.settings.entitlement.isPro
        return DebugSection(title: "Pro", actions: [
            DebugAction(title: "Toggle Pro", detail: "Now: \(isPro ? "Pro" : "Free")") { context in
                context.ledgerStore.setPro(!isPro)
            },
            DebugAction(title: "Clear the entitlement") { context in
                context.ledgerStore.updateSettings { $0.entitlement = Entitlement() }
            },
        ])
    }

    /// Each row runs the store action a real sync would, "as" the friend. A row shows only when the
    /// data it needs is there.
    static func friendsSide(_ ledger: Ledger) -> DebugSection {
        let pending = ledger.payments.last { $0.fromId == Person.me && $0.status == .pending }
        let payee = pending.flatMap { ledger.person($0.toId)?.firstName } ?? "Payee"
        var actions: [DebugAction] = []
        if ledger.person(esha) != nil {
            actions.append(claim(from: esha, title: "Esha says she paid ₹700", detail: "A pending claim for Dinner at Olive Garden"))
        }
        if let pending {
            actions.append(DebugAction(title: "\(payee) confirms my latest pending payment", detail: Money.format(pending.amount, pending.currency)) { context in
                try context.ledgerStore.confirmPayment(pending.id)
            })
            actions.append(DebugAction(title: "\(payee) says Not received") { context in
                try context.ledgerStore.markNotReceived(pending.id, note: "Hi, I haven’t received it yet. Could you check?")
            })
        }
        let seafoodDinner = ledger.expense(seafood)
        if let seafoodDinner, seafoodDinner.flag == nil {
            actions.append(DebugAction(title: "Esha flags Seafood dinner") { context in
                try context.ledgerStore.flagExpense(seafood, by: esha, note: "I left before dessert. Can we check the bill?")
            })
        }
        if seafoodDinner?.flag != nil {
            actions.append(DebugAction(title: "Esha removes her flag") { context in
                try context.ledgerStore.resolveFlag(seafood, by: esha)
            })
        }
        if ledger.expense(villa) != nil {
            actions.append(DebugAction(title: "Priya comments on Villa") { context in
                try context.ledgerStore.addComment(to: villa, text: "Thanks, that works for me.", by: priya)
            })
        }
        if ledger.group(flat302) != nil {
            actions.append(DebugAction(title: "Meera adds an expense in Flat 302", detail: "Groceries, ₹900") { context in
                try meeraAddsGroceries(context.ledgerStore)
            })
        }
        return DebugSection(title: "Friend’s side", actions: actions)
    }

    /// "Apply <name>" for every scenario the bundled demo defines, on today's ledger.
    static var scenarios: DebugSection {
        DebugSection(title: "Scenarios", actions: LedgerStore.scenarioNames.map { name in
            DebugAction(title: "Apply \(name)") { context in
                try context.ledgerStore.applyScenario(name)
            }
        })
    }

    /// The component gallery, then every start-screen id, opened as the launch hook would.
    static var loadScreen: DebugSection {
        let gallery = DebugAction(title: "Component gallery") { context in
            context.router.showGallery(page: 1)
        }
        return DebugSection(title: "Load screen", actions: [gallery] + ScreenID.allCases.map { screen in
            DebugAction(title: screen.rawValue) { context in
                DebugStart.show(screen, profileStore: context.profileStore, ledgerStore: context.ledgerStore, router: context.router)
            }
        })
    }

    /// `friend` records paying you ₹700 by UPI for Dinner at Olive Garden (when it exists), pending
    /// your Confirm, and the claim notification goes out.
    static func claim(from friend: PersonID, title: String, detail: String) -> DebugAction {
        DebugAction(title: title, detail: detail) { context in
            let store = context.ledgerStore
            let id = try store.recordPayment(PaymentDraft(
                fromId: friend, toId: Person.me, amount: 70_000, currency: "INR", method: .upi, date: store.clock.today,
                expenseId: store.ledger.expense(olive) == nil ? nil : olive, recordedBy: friend
            ))
            if let claim = store.snapshot.home.pendingClaims.first(where: { $0.id == id }) {
                Task { await NotificationService.postClaim(claim) }
            }
        }
    }

    /// Meera's own ₹900 of groceries in Flat 302, split equally among its members; the store adds the
    /// "New expense in Flat 302" inbox item a real sync would bring.
    private static func meeraAddsGroceries(_ store: LedgerStore) throws {
        guard let group = store.ledger.group(flat302) else { return }
        let amount: Int64 = 90_000
        try store.addExpense(ExpenseDraft(groupId: group.id, title: "Groceries", category: .food, amount: amount, currency: group.currency,
                                          date: store.clock.today, payers: [Payer(personId: meera, amount: amount)],
                                          rows: group.memberIds.map { SplitRow(personId: $0) }), by: meera)
    }
}
#endif
