import Foundation
import Testing
@testable import paybak

/// Add & Record's logic (lane A, M3): quick due dates, amount input, the split preview and its
/// footers, and the expense, payment and loan read models on the demo at Figma parity.
struct AddRecordTests {
    // MARK: Dates and input

    @Test func quickDueFromWednesday() {
        let today = DemoFixture.day(2026, 9, 30)
        #expect(QuickDue.tomorrow.day(from: today) == DemoFixture.day(2026, 10, 1))
        #expect(QuickDue.weekend.day(from: today) == DemoFixture.day(2026, 10, 4))
        #expect(QuickDue.nextWeek.day(from: today) == DemoFixture.day(2026, 10, 7))
        #expect(QuickDue.weekend.day(from: DemoFixture.day(2026, 10, 3)) == DemoFixture.day(2026, 10, 4))
        #expect(QuickDue.weekend.day(from: DemoFixture.day(2026, 10, 4)) == DemoFixture.day(2026, 10, 4))
        #expect(QuickDue.matching(DemoFixture.day(2026, 10, 4), today: today) == .weekend)
        #expect(Format.relativeDays(DemoFixture.day(2026, 10, 4), today: today) == "in 4 days")
        #expect(Format.relativeDays(DemoFixture.day(2026, 9, 28), today: today) == "2 days ago")
        #expect(Format.dateChip(today, today: today) == "Today")
        #expect(Format.dateChip(DemoFixture.day(2026, 9, 28), today: today) == "Mon 28 Sep")
    }

    @Test func moneyInput() {
        #expect(MoneyInput.minor("2800", currency: "INR") == 280_000)
        #expect(MoneyInput.minor("2800.5", currency: "INR") == 280_050)
        #expect(MoneyInput.minor("1200", currency: "JPY") == 1200)
        #expect(MoneyInput.text(280_050, currency: "INR") == "2800.5")
        #expect(MoneyInput.text(280_000, currency: "INR") == "2800")
        #expect(MoneyInput.basisPoints("33.33") == 3333)
        #expect(MoneyInput.percentText(2500) == "25")
        #expect(MoneyInput.percentText(1250) == "12.5")
        #expect(MoneyInput.grouped("2800", currency: "INR") == "2,800")
        #expect(MoneyInput.grouped("280000.5", currency: "INR") == "2,80,000.5")
        #expect(MoneyInput.grouped("280000.", currency: "USD") == "280,000.")
        #expect(MoneyInput.grouped("0.5", currency: "INR") == "0.5")
    }

    @Test func todaysRate() throws {
        let table = RateTable(inrPerUnit: ["INR": "1", "AED": "22.85", "USD": "83.92"])
        let rate = try #require(table.rate(from: "AED", to: "INR"))
        #expect(rate == Rate(value: "22.85", to: "INR"))
        #expect(table.rate(from: "INR", to: "INR") == nil)
        #expect(Money.approximateLine(120_000, currency: "AED", rate: rate) == "≈ ₹27,420 · ₹22.85 per AED")
    }

    // MARK: Split preview (add-expense §3.5)

    private static let olivePeople = [Person.me, "p-priya", "p-esha", "p-dev"]

    private func oliveDraft(_ mode: SplitMode = .equal, values: [PersonID: Int64] = [:]) -> ExpenseDraft {
        ExpenseDraft(title: "Dinner at Olive Garden", category: .food, amount: rupees(2800), currency: "INR",
                     date: DemoFixture.figmaDay, splitMode: mode,
                     rows: Self.olivePeople.map { SplitRow(personId: $0, value: values[$0]) })
    }

    @Test func equalPreview() {
        let preview = DemoFixture.load().previewSplit(oliveDraft())
        #expect(Set(preview.shares.values) == [rupees(700)])
        #expect(preview.formValue.text == "Equally · ₹700 each")
        #expect(preview.footer.left == "₹0 left")
        #expect(preview.footer.detail == "₹2,800 of ₹2,800")
    }

    @Test func exactErrorPreview() {
        let values = [Person.me: rupees(700), "p-priya": rupees(700), "p-esha": rupees(700), "p-dev": rupees(550)]
        let preview = DemoFixture.load().previewSplit(oliveDraft(.exact, values: values))
        #expect(!preview.isBalanced)
        #expect(preview.footer.left == "₹150 left")
        #expect(preview.footer.detail == "₹2,650 of ₹2,800")
        #expect(preview.formValue == ("Doesn’t add up", true))
    }

    @Test func percentAndSharesPreview() {
        let books = DemoFixture.load()
        let percent = books.previewSplit(oliveDraft(.percent, values: Dictionary(uniqueKeysWithValues: Self.olivePeople.map { ($0, 2500) })))
        #expect(percent.isBalanced)
        #expect(percent.formValue.text == "Percent · 4 people")
        let short = books.previewSplit(oliveDraft(.percent, values: [Person.me: 2500, "p-priya": 2500, "p-esha": 2500, "p-dev": 2000]))
        #expect(short.footer.left == "5% left")
        #expect(short.footer.detail == "95% of 100%")
        let shares = books.previewSplit(oliveDraft(.shares, values: [Person.me: 2, "p-priya": 1, "p-esha": 1, "p-dev": 0]))
        #expect(shares.shares[Person.me] == rupees(1400))
        #expect(shares.shares["p-dev"] == 0)
    }

    /// The preview is what Save stores, leftover paisa included, and the rotation then moves on.
    @Test func previewMatchesSaved() throws {
        var books = DemoFixture.load()
        let people = [Person.me, "p-priya", "p-esha"]
        let draft = ExpenseDraft(title: "Cab", amount: rupees(1000), currency: "INR", date: DemoFixture.figmaDay,
                                 rows: people.map { SplitRow(personId: $0) })
        let first = books.previewSplit(draft)
        #expect(first.formValue.text == "Equally · 3 people")
        let id = try books.addExpense(draft, at: DemoFixture.figmaNow)
        let saved = try #require(books.ledger.expense(id))
        #expect(Dictionary(uniqueKeysWithValues: saved.split.rows.map { ($0.personId, $0.share) }) == first.shares)
        #expect(books.previewSplit(draft).shares != first.shares)
    }

    @Test func splitEditorPercentStart() {
        let points = SplitEditorState.basisPoints(of: ["a": 33_334, "b": 33_333, "c": 33_333], among: ["a", "b", "c"], total: 100_000)
        #expect(points.values.reduce(0, +) == 10_000)
        #expect(points["a"] == 3334)
    }

    // MARK: Saving changes the totals (M3 acceptance)

    @Test func addingADinnerAddsWhatOthersOwe() throws {
        var books = DemoFixture.load()
        let before = books.homeTotals()
        try books.addExpense(oliveDraft(), at: DemoFixture.figmaNow)
        let after = books.homeTotals()
        #expect(after.owed - before.owed == rupees(2100))
        #expect(after.owedPeople == before.owedPeople)
    }

    // MARK: Expense detail (activity §4, add-expense §11)

    @Test func oliveGardenDetail() throws {
        let detail = try #require(DemoFixture.load().expenseDetail("e-olive"))
        #expect(detail.meta == "Paid by you · Today")
        #expect(detail.yourShare == "₹700")
        #expect(detail.due == "Sun 4 Oct")
        #expect(detail.groupBalance == nil)
        #expect(detail.groupName == nil)
        #expect(detail.splitHeader == "Split equally · 4 people")
        #expect(detail.splitLines.map(\.name) == ["You", "Priya", "Esha", "Dev"])
        #expect(detail.splitLines.first?.subtitle == "Paid ₹2,800")
        #expect(detail.history.map(\.text) == ["You added this"])
        #expect(detail.history.first?.date == "Today")
        #expect(!detail.canFlag)
    }

    @Test func villaDetail() throws {
        let detail = try #require(DemoFixture.load().expenseDetail("e-goa-villa"))
        #expect(detail.meta == "Paid by Kabir · 21 Sep")
        #expect(detail.yourShare == "₹3,600")
        #expect(detail.due == "Fri 2 Oct")
        #expect(detail.groupBalance?.title == "Your Goa Trip balance")
        #expect(detail.groupBalance?.net == -rupees(1400))
        #expect(detail.splitLines.map(\.name) == ["Kabir", "You", "Priya", "Esha", "Dev"])
        #expect(detail.history.map(\.text) == ["Kabir changed the amount from ₹17,500 to ₹18,000", "Kabir added this"])
        #expect(detail.receiptCaption == "Added by Kabir · 21 Sep")
        #expect(detail.canFlag)
    }

    // MARK: Payments (record-lend-group §2–§3)

    @Test func recordPaymentPrefill() throws {
        let books = DemoFixture.load()
        let debt = try #require(books.suggestedPayment())
        #expect(debt.creditor == "p-meera")
        #expect(debt.amount == rupees(450))
        #expect(books.openBalanceHelper(with: "p-meera", for: .group("g-flat302")) == "You owe Meera ₹450 in Flat 302")
        #expect(books.openBalanceHelper(with: "p-rohan", for: .direct(expense: nil)) == "Rohan owes you ₹800")
        #expect(books.paymentSummary(from: Person.me, to: "p-meera", amount: rupees(450), currency: "INR", method: .cash,
                                     context: .group("g-flat302"))
            == "You paid Meera ₹450 in cash for Flat 302.\nMeera will be asked to confirm. Paybak never moves money.")
        #expect(books.paymentSummary(from: Person.me, to: "p-kabir", amount: rupees(1400), currency: "INR", method: .upi,
                                     context: .group("g-goa")).hasPrefix("You paid Kabir ₹1,400 by UPI for Goa Trip."))
    }

    @Test func pendingPaymentDetail() throws {
        let books = DemoFixture.load("paymentToMeeraPending")
        let detail = try #require(books.paymentDetail("pay-me-meera", myUPI: "arjun@okaxis"))
        #expect(detail.title == "You paid Meera")
        #expect(detail.meta == "Cash · Today · Flat 302")
        #expect(detail.notice == .awaitingThem(name: "Meera"))
        #expect(detail.rows.map(\.title) == ["From", "To", "Method", "Date", "For", "Proof"])
        #expect(detail.rows.map(\.value) == ["You", "Meera", "Cash", "Wed 30 Sep", "Flat 302", "None"])
        #expect(detail.footnote == "Your balance updates once Meera confirms.")
        #expect(detail.canChange)
        #expect(books.cancelPaymentMessage(detail.payment) == "Meera won’t be asked to confirm. You’ll still owe her ₹450.")
        // Pending changes nothing.
        #expect(books.homeTotals().owe == rupees(1850))
    }

    @Test func upiPaymentShowsPaidTo() throws {
        let detail = try #require(DemoFixture.load("paymentToKabirPending").paymentDetail("pay-me-kabir", myUPI: nil))
        #expect(detail.rows.count == 7)
        #expect(detail.rows[3] == PaymentDetail.Row(title: "Paid to", value: "kabir@okaxis"))
    }

    // MARK: Loans (record-lend-group §5, domain.md §9)

    @Test func kabirLoanPaidBack() throws {
        let detail = try #require(DemoFixture.load().loanDetail("l-kabir-bike"))
        #expect(detail.title == "You lent Kabir")
        #expect(detail.meta == "Bike service · 12 Jun")
        #expect(detail.isPaidBack)
        #expect(detail.caption == "Paid back on 14 Sep")
        #expect(detail.lines.map(\.subtitle) == ["Paid 10 Jul", "Paid 12 Aug", "Paid 14 Sep · 2 days late"])
        #expect(detail.sectionTitle == "3 monthly installments")
    }

    @Test func devLoanAddedAndOverdue() throws {
        let added = try #require(DemoFixture.load("lendDev").loanDetail("l-dev-laptop"))
        #expect(added.meta == "Laptop repair · Today")
        #expect(added.caption == "0% paid back")
        #expect(added.lines.map(\.subtitle) == ["Due Fri 30 Oct", "Due Mon 30 Nov", "Due Wed 30 Dec"])
        #expect(added.lines.allSatisfy { $0.overdue == nil })
        #expect(added.nextAmount == rupees(2000))
        let overdue = try #require(DemoFixture.load("lendDevOverdue").loanDetail("l-dev-laptop"))
        #expect(overdue.meta == "Laptop repair · Wed 30 Sep")
        #expect(overdue.lines.map(\.overdue) == ["Overdue 4 days", nil, nil])
        #expect(overdue.lastReminder == "Last reminder sent Mon 2 Nov")
    }

    @Test func schedulePreview() {
        #expect(Books.schedulePreview(amount: rupees(6000), currency: "INR", count: 3, frequency: .monthly, firstDue: DemoFixture.day(2026, 10, 30))
            == "3 × ₹2,000 · Fri 30 Oct, Mon 30 Nov and Wed 30 Dec")
        #expect(Books.schedulePreview(amount: rupees(6000), currency: "INR", count: 6, frequency: .monthly, firstDue: DemoFixture.day(2026, 10, 30))
            == "6 × ₹1,000 · monthly from Fri 30 Oct to Tue 30 Mar")
    }

    // MARK: Forms

    @MainActor
    @Test func expenseFormSaveRule() {
        let form = ExpenseForm(draft: ExpenseDraft(currency: "INR", date: DemoFixture.figmaDay, rows: [SplitRow(personId: Person.me)]),
                               categoryChosen: false)
        let books = DemoFixture.load()
        form.amountText = "2800"
        #expect(!form.canSave(split: books.previewSplit(form.draft)))
        form.setPeople(Self.olivePeople)
        #expect(form.canSave(split: books.previewSplit(form.draft)))
        #expect(form.draft.category == .other)
        form.splitMode = .exact
        form.splitValues = [Person.me: rupees(700)]
        #expect(!form.canSave(split: books.previewSplit(form.draft)))
    }

    @MainActor
    @Test func groupFormShares() {
        let form = GroupForm(mode: .project, currency: "INR")
        form.setMembers(["p-esha", "p-dev", "p-kabir"])
        #expect(form.equalShareText == "25%")
        form.setMembers(["p-esha", "p-dev"])
        #expect(form.equalShareText == "33.3%")
        #expect(!form.canCreate)
        form.name = "Weekend Trek"
        form.setRule(.percent)
        #expect(form.contributionAddsUp)
        form.shares[Person.me] = "50"
        #expect(!form.canCreate)
        form.mode = .group
        form.type = .trip
        #expect(form.draft.icon == "plane")
        #expect(form.draft.memberIds == [Person.me, "p-esha", "p-dev"])
    }
}
