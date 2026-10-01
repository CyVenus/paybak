import StoreKit
import SwiftUI

/// Help & feedback (screens-settings §11): the five common questions (each opens its answer), Contact
/// us (the mail composer, with the version in the body), Rate Paybak (the review prompt) and the
/// version from the build.
struct HelpScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(\.openURL) private var openURL
    @Environment(\.requestReview) private var requestReview

    var body: some View {
        SettingsScaffold(title: "Help & feedback", testIDPrefix: "helpFeedback") {
            SettingsSection(title: "Common questions") {
                VStack(spacing: 0) {
                    ForEach(HelpFAQ.all.indices, id: \.self) { index in
                        PBSettingRow(HelpFAQ.all[index].question, icon: .help, showsDivider: index < HelpFAQ.all.count - 1,
                                     titleLineLimit: 2) { router.open(.helpAnswer(index: index)) }
                            .accessibilityIdentifier("helpFeedback.faq.\(index)")
                    }
                }
                .pbCard(padding: 0)
            }
            SettingsSection(title: "Get in touch") {
                VStack(spacing: 0) {
                    PBSettingRow("Contact us", icon: .mail, action: contactUs)
                        .accessibilityIdentifier("helpFeedback.contact")
                    PBSettingRow("Rate Paybak", icon: .star, showsDivider: false) { requestReview() }
                        .accessibilityIdentifier("helpFeedback.rate")
                }
                .pbCard(padding: 0)
            }
            Text(SupportContact.versionLine)
                .textStyle(.footnote)
                .foregroundStyle(PBColor.textTertiary)
                .frame(maxWidth: .infinity)
                .accessibilityIdentifier("helpFeedback.version")
        }
    }

    private func contactUs() {
        openURL(SupportContact.mailURL) { accepted in
            if !accepted {
                router.toast("Couldn’t open Mail.")
            }
        }
    }
}

/// Where feedback goes (app-architecture §8: the address is a proposal, kept in this one place).
enum SupportContact {
    static let email = "support@paybak.app"

    /// "Paybak 1.0 (1)" from the build.
    static var versionLine: String {
        let info = Bundle.main.infoDictionary
        let version = info?["CFBundleShortVersionString"] as? String ?? "1.0"
        let build = info?["CFBundleVersion"] as? String ?? "1"
        return "Paybak \(version) (\(build))"
    }

    /// A new mail with the subject "Paybak feedback" and the version line in the body.
    static var mailURL: URL {
        var components = URLComponents()
        components.scheme = "mailto"
        components.path = email
        components.queryItems = [
            URLQueryItem(name: "subject", value: "Paybak feedback"),
            URLQueryItem(name: "body", value: "\n\n\(versionLine)"),
        ]
        return components.url ?? URL(string: "mailto:\(email)")!
    }
}

#Preview("HelpScreen") {
    HelpScreen()
        .environment(AppRouter())
}
