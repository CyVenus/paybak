import Foundation
import Testing
@testable import paybak

/// The main app's navigation helpers (app-architecture §2.7) and deep links (§2.6).
@MainActor
struct RouterTests {
    private func mainRouter() -> AppRouter {
        let router = AppRouter()
        router.showMain()
        return router
    }

    @Test func openFollowsThePresentation() {
        let router = mainRouter()
        router.open(.group("g-goa"))
        router.open(.addSheet)
        #expect(router.mainPath == [.group("g-goa")])
        #expect(router.mainSheet == .addSheet)
        router.dismissSheet()
        router.open(.addExpense(.new))
        router.open(.pickPeople(PeoplePickRequest(id: "r1")))
        router.open(.pickCurrency(CurrencyPickRequest(id: "r2")))
        #expect(router.modals.map(\.root) == [.addExpense(.new)])
        #expect(router.modals[0].path == [.pickPeople(PeoplePickRequest(id: "r1"))])
        #expect(router.modals[0].sheet == .pickCurrency(CurrencyPickRequest(id: "r2")))
        router.open(.activity)
        #expect(router.selectedTab == .activity)
        #expect(router.modals.isEmpty && router.mainPath.isEmpty && router.mainSheet == nil)
    }

    @Test func backPopsThenDismisses() {
        let router = mainRouter()
        router.open(.recordPayment(.new))
        router.open(.pickPeople(PeoplePickRequest()))
        router.back()
        #expect(router.modals.count == 1 && router.modals[0].path.isEmpty)
        router.back()
        #expect(router.modals.isEmpty)
    }

    @Test func replaceSheetOpensAfterTheSheetGoes() {
        let router = mainRouter()
        router.open(.addSheet)
        router.replaceSheet(with: .addExpense(.new))
        #expect(router.mainSheet == nil)
        #expect(router.modals.isEmpty)
        router.sheetDidDismiss()
        #expect(router.modals.map(\.root) == [.addExpense(.new)])
    }

    @Test func didSavePushesUnderTheModalAndToasts() {
        let router = mainRouter()
        router.open(.addExpense(.new))
        router.didSave(.expense("e-1"), toast: "Expense added")
        #expect(router.modals.isEmpty)
        #expect(router.mainPath == [.expense("e-1")])
        #expect(router.toast?.text == "Expense added")
    }

    @Test func didCreateGroupLandsOnGroups() {
        let router = mainRouter()
        router.open(.newGroup(.project))
        router.didCreateGroup("pj-1", isProject: true)
        #expect(router.selectedTab == .groups)
        #expect(router.modals.isEmpty)
        #expect(router.mainPath == [.project("pj-1")])
        #expect(router.toast?.text == "Project created")
    }

    @Test func requireProAndFinishPaywall() {
        let router = mainRouter()
        router.requirePro(.ask)
        #expect(router.modals.map(\.root) == [.paywall(continueTo: .ask)])
        router.finishPaywall()
        #expect(router.modals.map(\.root) == [.ask])
        router.dismissModal()
        router.isPro = { true }
        router.requirePro(.privacyExport)
        #expect(router.mainPath == [.privacyExport])
    }

    @Test func pickerResultsReachTheCaller() {
        let router = mainRouter()
        router.open(.addExpense(.new))
        router.open(.pickCurrency(CurrencyPickRequest(id: "currency")))
        router.complete("currency", with: .currency("AED"))
        #expect(router.modals[0].sheet == nil)
        #expect(router.takeResult("currency") == .currency("AED"))
        #expect(router.takeResult("currency") == nil)
    }

    @Test func deepLinks() {
        #expect(DeepLink("paybak://activity?claim=pay-1&action=notReceived") == .claim("pay-1", notReceived: true))
        #expect(DeepLink("paybak://record-payment?to=p-kabir&amount=140000&context=group:g-goa")
            == .recordPayment(to: "p-kabir", amount: 140_000, context: .group("g-goa")))
        #expect(DeepLink("paybak://insights?month=2026-09") == .insights(YearMonth(year: 2026, month: 9)))
        #expect(DeepLink("paybak://expense/e-goa-villa") == .expense("e-goa-villa"))
        #expect(DeepLink("https://example.com") == nil)
        for link in [DeepLink.claim("p", notReceived: false), .recordPayment(to: "p", amount: 5, context: .loan("l")), .remind("p"),
                     .recurringDraft("d"), .payment("x"), .insights(YearMonth(year: 2026, month: 9))] {
            #expect(DeepLink(link.text) == link)
        }
        let router = mainRouter()
        router.open(DeepLink.claim("pay-1", notReceived: true))
        #expect(router.selectedTab == .activity)
        #expect(router.mainSheet == .notReceived("pay-1"))
    }

    @Test func routesRoundTripThroughJSON() throws {
        let routes: [Route] = [.expense("e", toast: "Expense added"), .paywall(continueTo: .privacyExport),
                               .addExpense(AddExpenseArgs(draft: ExpenseDraft(currency: "INR", date: LocalDay(year: 2026, month: 9, day: 30)))),
                               .activityLog(.category(.food, month: YearMonth(year: 2026, month: 9)))]
        let decoded = try JSONDecoder().decode([Route].self, from: JSONEncoder().encode(routes))
        #expect(decoded == routes)
    }
}
