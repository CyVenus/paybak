package app.paybak.paybak.debug.scenarios

import app.paybak.paybak.domain.model.ReminderContext
import app.paybak.paybak.navigation.Route

/** Settle up (lane B, M5; app-architecture §1.5). */
internal val SettleScenarios: Map<String, Scenario> =
    mapOf(
        "settleOwedBreakdown" to Scenario(demo(), stack = listOf(Route.OwedBreakdown)),
        "settleOweBreakdown" to Scenario(demo(), stack = listOf(Route.OweBreakdown)),
        "settleUp" to Scenario(demo(), stack = listOf(Route.SettleUp())),
        "settleRemind" to
            Scenario(
                base(),
                sheet = Route.Remind("p-rohan", ReminderContext(expenseId = "e-movie")),
            ),
        "settleRemindShare" to
            Scenario(
                base(),
                sheet = Route.Remind("p-rohan", ReminderContext(expenseId = "e-movie")),
            ),
        "settleNotReceived" to Scenario(demo(), sheet = Route.NotReceived("pay-esha-olive")),
    )
