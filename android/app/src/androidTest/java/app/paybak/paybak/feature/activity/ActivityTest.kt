package app.paybak.paybak.feature.activity

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.paybak.paybak.awaitScreen
import app.paybak.paybak.launchPaybak
import app.paybak.paybak.paybakApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val CLAIM = "claim.pay-esha-olive"
private const val VILLA = "e-goa-villa"

/** Activity, the expense detail's extras and the inbox (app-architecture §6.2, M6 UI tests). */
@RunWith(AndroidJUnit4::class)
class ActivityTest {
    @get:Rule val compose = createEmptyComposeRule()

    private fun tag(tag: String) = compose.onNodeWithTag(tag, useUnmergedTree = true)

    private fun count(tag: String) =
        compose.onAllNodes(hasTestTag(tag), useUnmergedTree = true).fetchSemanticsNodes().size

    private fun await(tag: String) = compose.waitUntil(10_000) { count(tag) > 0 }

    private fun awaitGone(tag: String) = compose.waitUntil(10_000) { count(tag) == 0 }

    private fun tap(tag: String) {
        await(tag)
        tag(tag).performClick()
    }

    /** [tap] for something further down a scrolling screen. */
    private fun scrollTap(tag: String) {
        await(tag)
        tag(tag).performScrollTo().performClick()
    }

    @Test
    fun timelineShowsTheClaimOnTopOfTodayThenTheDays() {
        launchPaybak("activityTimeline").use {
            compose.awaitScreen("activity")
            await(CLAIM)
            val claim = tag(CLAIM).fetchSemanticsNode().boundsInRoot
            val reminder =
                tag("activity.row.rem-e-movie-20260930").fetchSemanticsNode().boundsInRoot
            val dinner = tag("activity.row.e-olive").fetchSemanticsNode().boundsInRoot
            assertTrue(claim.bottom <= reminder.top && reminder.bottom <= dinner.top)
            compose.onNodeWithText("Today").assertExists()
            compose.onNodeWithText("Yesterday").assertExists()
            compose.onNodeWithText("Mon 28 Sep").assertExists()
            compose.onNodeWithText("Goa Trip · Was ₹17,500").assertExists()
            tag("activity.row.e-goa-villa.edited").performScrollTo().performClick()
            compose.awaitScreen("expense")
            compose.onNodeWithText("Villa (3 nights)").assertExists()
        }
    }

    @Test
    fun aNewAccountShowsTheFirstDayCard() {
        launchPaybak("activityEmpty").use {
            compose.awaitScreen("activity")
            await("activity.empty")
            compose.onNodeWithText("No activity yet.").assertExists()
            assertEquals(0, count("activity.recentlyDeleted"))
        }
    }

    @Test
    fun confirmingOnActivitySettlesItOnHome() {
        launchPaybak("activityTimeline").use {
            compose.awaitScreen("activity")
            tap("$CLAIM.confirm")
            awaitGone(CLAIM)
            tag("activity.row.pay-esha-olive").assertExists()
            tap("home.tab.home")
            compose.awaitScreen("homeActive")
            compose.onNodeWithText("+₹2,200").assertExists()
            compose.onNodeWithText("from 3 people").assertExists()
        }
    }

    @Test
    fun deletedVillaWaitsInRecentlyDeletedAndComesBack() {
        launchPaybak("expenseVilla").use {
            compose.awaitScreen("expense")
            scrollTap("expense.delete")
            tap("expense.alert.action")
            compose.awaitScreen("activity")
            assertTrue(paybakApp.ledger.ledger.value.expense(VILLA)?.deletedAt != null)
            tap("activity.recentlyDeleted")
            compose.awaitScreen("recentlyDeleted")
            compose.onNodeWithText("Deleted by you on 30 Sep · 30 days left").assertExists()
            tap("recentlyDeleted.restore.$VILLA")
            awaitGone("recentlyDeleted.row.$VILLA")
            tag("toast").assertExists()
            val villa = paybakApp.ledger.ledger.value.expense(VILLA)
            assertNull(villa?.deletedAt)
            assertEquals("g-goa", villa?.groupId)
        }
    }

    @Test
    fun aSentCommentAppearsAndTheComposerClears() {
        launchPaybak("expenseVilla").use {
            compose.awaitScreen("expense")
            scrollTap("expense.composer")
            await("expense.composer")
            tag("expense.composer").performTextReplacement("Booked the cab too.")
            compose.onNodeWithContentDescription("Send").performClick()
            // Once in the comments, and no longer in the cleared composer.
            compose.waitUntil(5_000) {
                compose.onAllNodes(hasText("Booked the cab too.")).fetchSemanticsNodes().size == 1
            }
            val comments = paybakApp.ledger.ledger.value.expense(VILLA)!!.comments
            assertEquals("Booked the cab too.", comments.last().text)
        }
    }

    @Test
    fun resolvingTheFlagClearsTheChipAndLogsIt() {
        launchPaybak("expenseDisputed").use {
            compose.awaitScreen("expense")
            compose.onNodeWithText("Disputed").assertExists()
            tap("expense.dispute.secondary")
            awaitGone("expense.dispute")
            compose.onNodeWithText("Disputed").assertDoesNotExist()
            compose.onNodeWithText("You resolved Esha’s flag").performScrollTo().assertExists()
        }
    }

    @Test
    fun markAllReadClearsTheBellDot() {
        launchPaybak("notifications").use {
            compose.awaitScreen("notifications")
            assertEquals(2, paybakApp.ledger.snapshot.value.unreadCount)
            tap("notifications.markAllRead")
            compose.waitUntil(5_000) { paybakApp.ledger.snapshot.value.unreadCount == 0 }
            tap("notifications.back")
            await("home.bell")
            compose.onNodeWithContentDescription("Notifications").assertExists()
            compose.onNodeWithContentDescription("Notifications, unread").assertDoesNotExist()
        }
    }

    @Test
    fun theReminderRowRecordsThePaymentToKabir() {
        launchPaybak("notifications").use {
            compose.awaitScreen("notifications")
            tap("notifications.row.n-reminder-g-goa-20260930")
            compose.awaitScreen("recordPayment")
            compose.onNodeWithText("₹1,400").assertExists()
            assertTrue(
                paybakApp.ledger.ledger.value.inbox
                    .first { it.id == "n-reminder-g-goa-20260930" }
                    .read
            )
        }
    }

    @Test
    fun notificationLinksOpenTheirScreens() {
        launchPaybak(
                "homeActive",
                link = "paybak://activity?claim=pay-esha-olive&action=notReceived",
                scenarios = listOf("eshaClaimsPayment"),
            )
            .use {
                compose.awaitScreen("activity")
                compose.awaitScreen("notReceived")
            }
        launchPaybak(
                "activityTimeline",
                link = "paybak://record-payment?to=p-kabir&amount=140000&context=group:g-goa",
            )
            .use {
                compose.awaitScreen("recordPayment")
                compose.onNodeWithText("₹1,400").assertExists()
            }
    }
}
