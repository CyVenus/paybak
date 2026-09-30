import Foundation
import Testing
@testable import paybak

@MainActor
struct CurrencyTests {
    private let english = Locale(identifier: "en_US")

    @Test func designedCurrenciesUseTheFigmaNames() {
        #expect(Currency(code: "AED", locale: english).name == "UAE Dirham")
        #expect(Currency(code: "SGD", locale: english).tileText == "S$")
        #expect(Currency(code: "AED", locale: english).tileText == "AED")
        #expect(Currency(code: "INR", locale: english).tileText == "₹")
    }

    @Test func suggestionFollowsTheRegion() {
        let india = Currency.suggested(for: Locale(identifier: "en_IN"))
        #expect(india.currency.code == "INR")
        #expect(india.isFromRegion)
        #expect(Currency.suggested(for: Locale(identifier: "en_GB")).currency.code == "GBP")
    }

    @Test func suggestionFallsBackToRupees() {
        let unknown = Currency.suggested(for: Locale(identifier: "en_001"))
        #expect(unknown.currency.code == "INR")
        #expect(!unknown.isFromRegion)
    }

    @Test func popularLeavesOutTheSuggestion() {
        #expect(Currency.popular(excluding: "INR").map(\.code) == ["USD", "EUR", "GBP", "AED", "SGD"])
        #expect(Currency.popular(excluding: "EUR").map(\.code) == ["USD", "GBP", "AED", "SGD"])
    }

    @Test func searchRanksCodeMatchesFirst() {
        let all = Currency.all(locale: english)
        let results = Currency.search("us", in: all).map(\.code)
        // "USD" starts with "us"; "Australian Dollar" and others only contain it in the name.
        #expect(results.first == "USD")
        #expect(results.contains("AUD"))

        #expect(Currency.search("inr", in: all).first?.code == "INR")
        #expect(Currency.search("  Rupee ", in: all).first?.name.hasPrefix("Indian") == true)
    }

    @Test func searchIgnoresCaseAndAccents() {
        let all = Currency.all(locale: english)
        let accented = Currency.search("CÓRDOBA", in: all).map(\.code)
        #expect(accented == Currency.search("cordoba", in: all).map(\.code))
        #expect(accented.contains("NIO"))
    }

    @Test func searchWithoutMatchesIsEmpty() {
        #expect(Currency.search("zzzz", in: Currency.all(locale: english)).isEmpty)
        #expect(Currency.search("   ", in: Currency.all(locale: english)).isEmpty)
    }

    @Test func searchTiesAreAlphabetical() {
        let all = Currency.all(locale: english)
        let dollars = Currency.search("dollar", in: all).map(\.name)
        #expect(dollars == dollars.sorted { $0.localizedStandardCompare($1) == .orderedAscending })
    }
}

@MainActor
struct UPIIDTests {
    @Test(arguments: ["arjun@okaxis", "a.b-c_d@ybl", "98765@paytm"])
    func acceptsUPIIDs(_ text: String) {
        #expect(UPIID.isValid(text))
    }

    @Test(arguments: ["", "arjun", "a@okaxis", "arjun@b", "arjun@ok axis", "arjun@ok.axis", "arjun okaxis@x1"])
    func rejectsOtherText(_ text: String) {
        #expect(!UPIID.isValid(text))
    }
}

@MainActor
struct UserProfileTests {
    @Test func firstNameAndInitials() {
        var profile = UserProfile()
        profile.name = "  Arjun   Mehta "
        #expect(profile.firstName == "Arjun")
        #expect(profile.initials == "AM")
        profile.name = "priya"
        #expect(profile.initials == "P")
        profile.name = "Rohan Kumar Rao"
        #expect(profile.initials == "RR")
    }
}
