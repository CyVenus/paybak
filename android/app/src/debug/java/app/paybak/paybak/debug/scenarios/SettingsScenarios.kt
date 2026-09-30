package app.paybak.paybak.debug.scenarios

import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.Tab

private fun settings(vararg stack: Route, seed: List<String> = demo()) =
    Scenario(seed, tab = Tab.Profile, stack = stack.toList())

/** Settings and Pro (lane C, M8; app-architecture §1.8). */
internal val SettingsScenarios: Map<String, Scenario> =
    mapOf(
        "paywall" to settings(Route.Paywall()),
        "proWelcome" to
            settings(
                Route.PrivacyData,
                Route.Paywall(continueTo = Route.PrivacyExport),
                seed = demo().pro(),
            ),
        "paymentDetails" to settings(Route.PaymentDetails),
        "paymentAddUpi" to settings(Route.PaymentDetails),
        "paymentAddUpiError" to settings(Route.PaymentDetails),
        "settingsCurrency" to settings(Route.SettingsCurrency),
        "settingsNotifications" to settings(Route.SettingsNotifications),
        "mutedFriends" to settings(Route.SettingsNotifications, Route.MutedFriends),
        "privacyData" to settings(Route.PrivacyData),
        "privacyExport" to settings(Route.PrivacyData, Route.PrivacyExport, seed = demo().pro()),
        "privacyDeleteBlocked" to settings(Route.PrivacyData),
        "helpFeedback" to settings(Route.HelpFeedback),
        "helpAnswer" to settings(Route.HelpFeedback, Route.HelpAnswer(1)),
    )
