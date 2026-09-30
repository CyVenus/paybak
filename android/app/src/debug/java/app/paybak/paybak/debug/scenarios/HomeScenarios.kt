package app.paybak.paybak.debug.scenarios

import app.paybak.paybak.navigation.Route

/** Home (lane D) and the shell's own screens (app-architecture §1.2). */
internal val HomeScenarios: Map<String, Scenario> =
    mapOf(
        "homeFirstDay" to Scenario(E),
        "homeActive" to Scenario(base()),
        "homeAllSettled" to Scenario(base("allSettled")),
        "homeConfirmPayment" to Scenario(demo()),
        "settlePaymentConfirmed" to
            Scenario(base("eshaPaymentConfirmed"), toast = "Payment confirmed"),
        "homeAddSheet" to Scenario(base(), sheet = Route.AddSheet),
        "debugMenu" to Scenario(demo(), sheet = Route.DebugMenu),
    )
