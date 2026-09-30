import Foundation
import Testing
@testable import paybak

@MainActor
struct SignInContactTests {
    @Test(arguments: ["arjun@example.com", "  a@b.co  ", "first.last+tag@mail.example.org"])
    func acceptsPlausibleEmails(_ input: String) throws {
        let contact = try #require(SignInContact(input))
        #expect(contact.method == .email)
        #expect(contact.value == input.trimmingCharacters(in: .whitespaces))
    }

    @Test(arguments: ["", "arjun", "arjun@", "arjun@example", "@example.com", "ar jun@example.com", "a@b@c.com", "a@.com"])
    func rejectsImplausibleEmails(_ input: String) {
        #expect(SignInContact(input) == nil)
    }

    @Test func phoneWithoutCountryCodeGetsPlus91() throws {
        let contact = try #require(SignInContact("98765 43210"))
        #expect(contact.method == .phone)
        #expect(contact.value == "+91 98765 43210")
    }

    @Test func phoneWithCountryCodeIsKeptAsTyped() throws {
        let contact = try #require(SignInContact(" +44 20-7946-0958 "))
        #expect(contact.method == .phone)
        #expect(contact.value == "+44 20-7946-0958")
    }

    @Test func phoneNeedsSevenDigits() {
        #expect(SignInContact("123 456") == nil)
        #expect(SignInContact("1234567")?.value == "+91 1234567")
    }

    @Test(arguments: ["98765 4321x", "(987) 654-3210", "98+7654321", "+ - -"])
    func rejectsOtherCharactersInPhones(_ input: String) {
        #expect(SignInContact(input) == nil)
    }
}

@MainActor
struct VerificationTests {
    @Test func onlyAllZerosIsCorrect() {
        #expect(VerificationCode.isCorrect("000000"))
        #expect(!VerificationCode.isCorrect("482917"))
        #expect(!VerificationCode.isCorrect("00000"))
    }

    @Test func countdownStartsAtThirtyAndEndsAtZero() {
        let sent = Date(timeIntervalSinceReferenceDate: 1_000)
        let countdown = ResendCountdown.started(at: sent)
        #expect(countdown.secondsLeft(at: sent) == 30)
        #expect(countdown.secondsLeft(at: sent.addingTimeInterval(0.4)) == 30)
        #expect(countdown.secondsLeft(at: sent.addingTimeInterval(6)) == 24)
        #expect(countdown.secondsLeft(at: sent.addingTimeInterval(29.5)) == 1)
        #expect(countdown.secondsLeft(at: sent.addingTimeInterval(30)) == 0)
        #expect(countdown.secondsLeft(at: sent.addingTimeInterval(95)) == 0)
    }

    @Test func nextTickIsTheNextWholeSecond() {
        let sent = Date(timeIntervalSinceReferenceDate: 1_000)
        let countdown = ResendCountdown.started(at: sent)
        #expect(countdown.nextTick(after: sent.addingTimeInterval(0.25)) == sent.addingTimeInterval(1))
        #expect(countdown.nextTick(after: sent.addingTimeInterval(29.5)) == sent.addingTimeInterval(30))
        #expect(countdown.nextTick(after: sent.addingTimeInterval(30)) == nil)
    }

    @Test func labelIsMinutesAndSeconds() {
        #expect(ResendCountdown.label(secondsLeft: 30) == "Resend code in 0:30")
        #expect(ResendCountdown.label(secondsLeft: 24) == "Resend code in 0:24")
        #expect(ResendCountdown.label(secondsLeft: 5) == "Resend code in 0:05")
        #expect(ResendCountdown.label(secondsLeft: 75) == "Resend code in 1:15")
    }
}
