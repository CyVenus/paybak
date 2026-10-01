import Foundation
import Testing
@testable import paybak

/// The project detail copy (screens-projects §3–§8) from the demo at Figma parity: every string the
/// four refs show comes out of the records.
struct ProjectPageTests {
    @Test func buildADroneOnTrack() throws {
        let page = try #require(DemoFixture.load().projectPage("pj-drone"))
        #expect(page.state == .active)
        #expect(page.isEditable)
        #expect(page.subtitle == "Project · 4 members · Active since 10 Aug")
        #expect(page.notice == nil)
        #expect(page.budget.spent == "₹52,000")
        #expect(page.budget.budget == "of ₹60,000")
        #expect(page.budget.percent == "87% used")
        #expect(page.budget.left == "₹8,000 left")
        #expect(page.budget.planned == "Planned items bring it to ₹58,000")
        #expect(abs(page.budget.progress - 52.0 / 60) < 0.0001)
        #expect(abs((page.budget.projected ?? 0) - 58.0 / 60) < 0.0001)
        #expect(!page.budget.isOver)
    }

    @Test func buildADroneComponents() throws {
        let page = try #require(DemoFixture.load().projectPage("pj-drone"))
        #expect(page.components.map(\.part.name)
            == ["GPS module", "Camera", "Transmitter", "ESCs and propellers", "Battery", "Flight controller", "Motors ×4", "Frame"])
        #expect(page.components.map(\.subtitle) == [
            "Est. ₹6,000", "Dev · Unplanned", "You · Est. ₹5,000", "You · Est. ₹8,000", "Rohan · Est. ₹5,000",
            "Priya · Est. ₹9,000", "Dev · Est. ₹12,000", "Dev · Est. ₹6,000",
        ])
        #expect(page.components.map(\.amount) == ["—", "₹7,500", "₹5,000", "₹8,000", "₹4,500", "₹9,000", "₹12,000", "₹6,000"])
        #expect(page.components.first?.payerId == nil)
        #expect(page.components.map(\.part.status.title) == ["Planned", "Bought", "Bought", "Bought", "Bought", "Bought", "Done", "Done"])
    }

    @Test func buildADroneFairShareAndPlan() throws {
        let page = try #require(DemoFixture.load().projectPage("pj-drone"))
        #expect(page.shareRule == "Equal split · ₹13,000 each so far")
        #expect(page.shares.map(\.name) == ["Dev", "You", "Priya", "Rohan"])
        #expect(page.shares.map(\.caption) == ["Paid ₹25,500", "Paid ₹13,000", "Paid ₹9,000", "Paid ₹4,500"])
        #expect(page.shares.map(\.value) == ["₹12,500", "Settled", "₹4,000", "₹8,500"])
        #expect(page.shares.map(\.tone) == [.owed, .settled, .owe, .owe])
        #expect(page.shares.map { Int(($0.fill * 100).rounded()) } == [100, 51, 35, 18])
        #expect(page.shares.allSatisfy { Int(($0.mark * 100).rounded()) == 51 })
        #expect(page.showsPlan)
        #expect(page.plan.map(\.title) == ["Rohan owes Dev", "Priya owes Dev"])
        #expect(page.plan.map(\.amount) == ["₹8,500", "₹4,000"])
        #expect(page.plan.map(\.role) == [.others, .others])
        #expect(page.footnote == "You’re settled in this project.")
        #expect(page.members.isEmpty)
    }

    @Test func overBudget() throws {
        let page = try #require(DemoFixture.load("devBuysGps").projectPage("pj-drone"))
        #expect(page.state == .over)
        #expect(page.budget.spent == "₹61,500")
        #expect(page.budget.isOver)
        #expect(page.budget.left == "₹1,500 over budget")
        #expect(page.budget.planned == "All planned items are bought.")
        #expect(page.budget.projected == nil)
        #expect(abs(page.budget.progress - 60.0 / 61.5) < 0.0001)
        #expect(page.components.first?.subtitle == "Dev · Est. ₹6,000")
        #expect(page.components.first?.amount == "₹9,500")
        #expect(page.components.first?.payerId == "p-dev")
        #expect(page.shareRule == "Equal split · ₹15,375 each so far")
        #expect(page.footnote == "You owe ₹2,375 in this project.")
    }

    @Test func closed() throws {
        let page = try #require(DemoFixture.load("closeDrone").projectPage("pj-drone"))
        #expect(page.state == .closed)
        #expect(!page.isEditable)
        #expect(page.subtitle == "Project · 4 members")
        #expect(page.notice == .init(title: "Closed · Read-only", message: "Components are locked. Payments can still be recorded."))
        #expect(page.budget.left == "₹8,000 under budget")
        #expect(page.budget.planned == nil)
        #expect(page.budget.projected == nil)
        #expect(page.shareRule == nil)
        #expect(page.shares.isEmpty)
        #expect(page.plan.map(\.title) == ["Rohan pays Dev", "Priya pays Dev"])
        #expect(page.planNotice == nil)
        #expect(page.footnote == "You’re settled. It becomes a permanent record once everyone has paid.")
        #expect(page.components.count == 8)
    }

    @Test func closedWithAPendingPayment() throws {
        var books = DemoFixture.load("closeDrone")
        try books.recordPayment(PaymentDraft(fromId: "p-rohan", toId: "p-dev", amount: rupees(8500), currency: "INR",
                                             date: books.today, groupId: "pj-drone", recordedBy: "p-rohan"), at: books.now)
        let page = try #require(books.projectPage("pj-drone"))
        #expect(page.plan.map(\.title) == ["Rohan pays Dev", "Priya pays Dev"])
        #expect(page.footnote == "You’re settled. It becomes a permanent record once everyone has paid. 1 payment is waiting for confirmation.")
    }

    @Test func hackathonKitArchived() throws {
        let page = try #require(DemoFixture.load().projectPage("pj-hackathon"))
        #expect(page.state == .archived)
        #expect(page.subtitle == "Project · 4 members · Closed 30 Aug")
        #expect(page.notice == .init(title: "Read-only", message: "Nothing here can be edited."))
        #expect(page.budget.spent == "₹18,400")
        #expect(page.budget.budget == "of ₹20,000")
        #expect(page.budget.percent == "92% used")
        #expect(page.budget.left == "₹1,600 under budget")
        #expect(page.plan.isEmpty)
        #expect(page.planNotice == .init(title: "Everyone is settled", message: "No payments left in this project."))
        #expect(page.footnote == nil)
        #expect(page.members.map(\.name) == ["You", "Esha", "Dev", "Kabir"])
        #expect(page.members.allSatisfy { $0.status == "Settled" })
    }

    /// You are "You": your debts lead to Record payment, your credits to Remind.
    @Test func yourTransfersNameYou() throws {
        var books = DemoFixture.load()
        try books.addComponent(to: "pj-drone", nameOnly("Spare props", actual: "20000", paidBy: "p-rohan"), at: books.now)
        let page = try #require(books.projectPage("pj-drone"))
        #expect(page.plan.contains { $0.title.hasPrefix("You owe ") && $0.role == .youPay })
    }

    private func nameOnly(_ name: String, actual: String, paidBy: PersonID) -> ComponentForm {
        var form = ComponentForm()
        form.name = name
        form.setActual(actual)
        form.paidBy = paidBy
        return form
    }
}
