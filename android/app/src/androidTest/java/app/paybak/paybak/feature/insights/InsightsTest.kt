package app.paybak.paybak.feature.insights

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.paybak.paybak.awaitScreen
import app.paybak.paybak.launchPaybak
import app.paybak.paybak.paybakApp
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Insights (insights §2, app-architecture §6.4 M9 UI tests). */
@RunWith(AndroidJUnit4::class)
class InsightsTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val robot = InsightsAiRobot(compose)

    @Test
    fun theFreePlanSeesTheLockUntilTheTrialStarts() {
        launchPaybak("insightsLocked").use {
            compose.awaitScreen("activity")
            robot.await("insights.locked")
            robot.tap("insights.notice.primary")
            compose.awaitScreen("paywall")
            robot.tap("paywall.plan.yearly")
            robot.tap("paywall.cta")
            robot.tap("proWelcome.done")
            robot.awaitGone("screen.paywall")
            robot.await("insights.total")
            robot.awaitGone("insights.locked")
            robot.tag("insights.total").assertTextEquals("₹23,300")
            assertTrue(paybakApp.ledger.snapshot.value.isPro)
        }
    }

    @Test
    fun theMonthArrowsChangeTheReport() {
        launchPaybak("insightsSeptember").use {
            robot.await("insights.total")
            robot.tag("insights.month").assertTextEquals("September 2026")
            robot.assertText("insights.trend", "Up 5% from August")
            robot.tap("insights.monthPrev")
            robot.tag("insights.month").assertTextEquals("August 2026")
            robot.tag("insights.total").assertTextEquals("₹22,200")
            robot.assertText("insights.trend", "Up 8% from July")
            robot.tap("insights.monthNext")
            robot.tag("insights.month").assertTextEquals("September 2026")
            robot.tag("insights.total").assertTextEquals("₹23,300")
        }
    }

    @Test
    fun aCategoryOpensItsActivityAndFriendsShowTheirShares() {
        launchPaybak("insightsSeptember").use {
            robot.scrollTap("insights.who.friends")
            robot.await("insights.friend.p-priya")
            robot.scrollTap("insights.category.rent")
            compose.awaitScreen("activityLog")
        }
    }
}
