import Foundation
import Testing
@testable import paybak

/// Project edits (screens-projects §1.5–§1.6, §5, §6.7): the component sheet's rules, adding,
/// editing and deleting parts, the contribution rule as typed, and the locks once closed.
struct ProjectEditsTests {
    private func form(_ name: String, estimate: String = "", actual: String = "", paidBy: PersonID = Person.me) -> ComponentForm {
        var form = ComponentForm()
        form.name = name
        form.estimate = estimate
        form.setActual(actual)
        form.paidBy = paidBy
        return form
    }

    @Test func sheetRules() {
        var form = ComponentForm()
        #expect(form.status == .planned && form.paidBy == Person.me && form.isEmpty)
        #expect(!form.canSave(currency: "INR"))
        form.name = "  Spare propellers "
        #expect(form.canSave(currency: "INR"))
        form.setActual("1200")
        #expect(form.status == .bought)
        form.setActual("")
        #expect(form.status == .planned)
        form.status = .done
        #expect(!form.canSave(currency: "INR"))
        form.setActual("800")
        #expect(form.status == .done && form.canSave(currency: "INR"))
        #expect(form.actualCost(currency: "INR") == rupees(800))
    }

    @Test func addingAPlannedPartFeedsOnlyTheProjection() throws {
        var books = DemoFixture.load()
        let id = try books.addComponent(to: "pj-drone", form("Spare propellers", estimate: "1500"), at: books.now)
        let page = try #require(books.projectPage("pj-drone"))
        #expect(page.components.first?.id == id)
        #expect(page.components.first?.subtitle == "Est. ₹1,500")
        #expect(page.components.count == 9)
        #expect(page.budget.spent == "₹52,000")
        #expect(page.budget.planned == "Planned items bring it to ₹59,500")
        #expect(page.shareRule == "Equal split · ₹13,000 each so far")
        #expect(books.ledger.components.first { $0.id == id }?.history.map(\.kind) == ["added"])
    }

    @Test func anActualCostChangesSpentAndShares() throws {
        var books = DemoFixture.load()
        try books.addComponent(to: "pj-drone", form("Gimbal", actual: "4000", paidBy: "p-priya"), at: books.now)
        let page = try #require(books.projectPage("pj-drone"))
        #expect(page.budget.spent == "₹56,000")
        #expect(page.budget.left == "₹4,000 left")
        #expect(page.shareRule == "Equal split · ₹14,000 each so far")
        #expect(page.components.first { $0.part.name == "Gimbal" }?.subtitle == "Priya · Unplanned")
        #expect(page.plan.map(\.title) == ["Rohan owes Dev", "You owe Dev", "Priya owes Dev"])
    }

    @Test func editingBuysThePlannedPart() throws {
        var books = DemoFixture.load()
        let gps = try #require(books.ledger.components.first { $0.id == "c-drone-gps" })
        var edit = ComponentForm(gps, currency: "INR")
        #expect(edit.estimate == "6000" && edit.actual.isEmpty && edit.status == .planned)
        edit.setActual("9500")
        edit.paidBy = "p-dev"
        try books.editComponent(gps.id, edit, at: books.now)
        let page = try #require(books.projectPage("pj-drone"))
        #expect(page.state == .over)
        #expect(page.budget.left == "₹1,500 over budget")
        #expect(page.components.first?.subtitle == "Dev · Est. ₹6,000")
        #expect(books.ledger.components.first { $0.id == gps.id }?.history.last?.kind == "bought")
    }

    @Test func deletingAPartTakesItsCostOff() throws {
        var books = DemoFixture.load()
        try books.deleteComponent("c-drone-camera")
        let page = try #require(books.projectPage("pj-drone"))
        #expect(page.budget.spent == "₹44,500")
        #expect(page.components.count == 7)
    }

    @Test func closedProjectsAreLocked() {
        var books = DemoFixture.load("closeDrone")
        #expect(throws: LedgerError.notAllowed) { try books.addComponent(to: "pj-drone", form("Late part"), at: books.now) }
        #expect(throws: LedgerError.notAllowed) { try books.deleteComponent("c-drone-gps") }
        #expect(throws: LedgerError.notAllowed) { try books.updateProject("pj-drone") { $0.budget = nil } }
    }

    @Test func percentRuleAppliesOnceItAddsUp() throws {
        var books = DemoFixture.load()
        let members = try #require(books.ledger.group("pj-drone")).memberIds
        var edit = ContributionEdit.prefill(.percent, members: members, budget: rupees(60000), spent: rupees(52000), currency: "INR")
        #expect(members.map { edit.typed[$0] } == ["25", "25", "25", "25"])
        #expect(edit.check(members: members, budget: rupees(60000), currency: "INR").isValid)
        edit.typed[Person.me] = "30"
        let over = edit.check(members: members, budget: rupees(60000), currency: "INR")
        #expect(!over.isValid)
        #expect(over.helper == "Set each person’s share. Shares must add up to 100%. 5% over.")
        edit.typed["p-dev"] = "20"
        #expect(edit.check(members: members, budget: rupees(60000), currency: "INR").isValid)
        let contribution = edit.contribution(members: members, currency: "INR")
        try books.updateProject("pj-drone") { $0.contribution = contribution }
        let page = try #require(books.projectPage("pj-drone"))
        #expect(page.shareRule == "Percent split · shares follow each person’s %")
        #expect(books.projectReport("pj-drone").share[Person.me] == rupees(15600))
        #expect(ContributionEdit(contribution, members: members, currency: "INR").typed == edit.typed)
    }

    @Test func fixedRuleMustMakeTheBudget() throws {
        let members: [PersonID] = [Person.me, "p-dev", "p-priya", "p-rohan"]
        var edit = ContributionEdit.prefill(.fixed, members: members, budget: rupees(60000), spent: rupees(52000), currency: "INR")
        #expect(members.map { edit.typed[$0] } == ["15000", "15000", "15000", "15000"])
        #expect(edit.check(members: members, budget: rupees(60000), currency: "INR").isValid)
        edit.typed["p-rohan"] = "10000"
        #expect(edit.check(members: members, budget: rupees(60000), currency: "INR").helper
            == "Set a fixed amount for each person. ₹5,000 of the budget left.")
        #expect(!edit.check(members: members, budget: nil, currency: "INR").helper.contains("left"))
    }

    @Test func equalShareText() {
        #expect(ContributionEdit.equalShareText(count: 4) == "25%")
        #expect(ContributionEdit.equalShareText(count: 3) == "33.3%")
        #expect(ContributionEdit.equalShareText(count: 6) == "16.7%")
    }
}
