package app.paybak.paybak.feature.groups

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.paybak.paybak.awaitScreen
import app.paybak.paybak.awaitTag
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.launchPaybak
import app.paybak.paybak.paybakApp
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Groups & Friends (app-architecture §6.3, M4 UI tests) on the demo at Figma parity. */
@RunWith(AndroidJUnit4::class)
class GroupsTest {
    @get:Rule val compose = createEmptyComposeRule()

    private fun tag(tag: String) = compose.onNodeWithTag(tag)

    /** Waits for [tag], scrolls it into view when it sits in a scrolling page, and taps it. */
    private fun tap(tag: String) {
        compose.awaitTag(tag)
        runCatching { tag(tag).performScrollTo() }
        tag(tag).performClick()
    }

    private fun awaitGone(tag: String) =
        compose.waitUntil(10_000) {
            compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isEmpty()
        }

    @Test
    fun segmentsSwitchBetweenGroupsAndFriends() {
        launchPaybak("groupsList").use {
            compose.awaitTag("groups.state.groupsList")
            tag("groups.row.g-goa").assertExists()
            tag("groups.archived").assertExists()
            tag("groups.segment.friends").performClick()
            compose.awaitTag("groups.state.friendsList")
            compose.onNodeWithText("+₹2,900").assertExists()
            compose.onNodeWithText("Overdue 3 days").assertExists()
            tag("groups.segment.groups").performClick()
            compose.awaitTag("groups.state.groupsList")
        }
    }

    @Test
    fun goaTripShowsItsBalancesAndLeaveIsBlocked() {
        launchPaybak("groupsList").use {
            tap("groups.row.g-goa")
            compose.awaitScreen("group")
            compose.onNodeWithText("You owe Kabir · Due Fri 2 Oct").assertExists()
            tag("group.balances.row.p-kabir").assertExists()
            tag("group.simplifyNote")
                .assertTextEquals(
                    "Simplify debts is on. You, Priya, Esha and Dev each pay Kabir directly."
                )
            tap("group.settings")
            compose.awaitScreen("groupSettings")
            tap("groupSettings.leave")
            compose.awaitTag("groupSettings.leaveBlocked")
            compose
                .onNodeWithText(
                    "You owe ₹1,400 in Goa Trip. Settle up with Kabir first, then you can leave."
                )
                .assertExists()
            tag("groupSettings.leaveBlocked.action").performClick()
            compose.awaitScreen("recordPayment")
        }
    }

    @Test
    fun youCanLeaveASettledGroup() {
        launchPaybak("groupDubaiWeekend").use {
            compose.awaitScreen("group")
            tap("group.settings")
            compose.awaitScreen("groupSettings")
            tap("groupSettings.leave")
            compose.awaitTag("groupSettings.leaveConfirm")
            tag("groupSettings.leaveConfirm.action").performClick()
            compose.awaitTag("groups.state.groupsList")
            awaitGone("groups.row.g-dubai")
            val dubai = paybakApp.ledger.ledger.value.groups.first { it.id == "g-dubai" }
            assertFalse(ME in dubai.memberIds)
        }
    }

    @Test
    fun rohansRemindOpensTheRemindSheet() {
        launchPaybak("friendsList").use {
            tap("friends.row.p-rohan")
            compose.awaitScreen("friend")
            compose.onNodeWithText("Last reminder sent today.").assertExists()
            tap("friend.remind")
            compose.awaitTag("screen.remind")
        }
    }

    @Test
    fun invitingAnanyaOpensHerGuestPage() {
        launchPaybak("addFriend").use {
            compose.awaitScreen("addFriend")
            tap("addFriend.invite.9876543210")
            compose.awaitScreen("friend")
            compose.onNodeWithText("Invite Ananya to Paybak").assertExists()
            tag("friend.noBalance").assertExists()
        }
    }

    @Test
    fun myQrCodeCopiesTheLink() {
        launchPaybak("addFriend").use {
            compose.awaitScreen("addFriend")
            tap("addFriend.myQr")
            compose.awaitTag("myQr.sheet")
            compose.onNodeWithText("paybak.app/i/arjun").assertExists()
            tap("myQr.copy")
            compose.awaitTag("toast")
            compose.onNodeWithText("Link copied").assertExists()
        }
    }

    @Test
    fun anEmptyAccountStartsANewGroup() {
        launchPaybak("groupsEmpty").use {
            compose.awaitTag("groups.empty")
            tap("groups.empty.newGroup")
            compose.awaitScreen("newGroup")
        }
    }

    @Test
    fun aNewGroupOffersItsFirstExpense() {
        launchPaybak("newGroupCreated").use {
            compose.awaitScreen("group")
            compose.onNodeWithText("Trip · 4 members · INR").assertExists()
            tap("group.addExpense")
            compose.awaitScreen("addExpense")
        }
    }
}
