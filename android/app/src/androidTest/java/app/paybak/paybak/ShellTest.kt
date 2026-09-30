package app.paybak.paybak

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.paybak.paybak.debug.ScreenIds
import app.paybak.paybak.debug.scenarios.Scenario
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.Tab
import app.paybak.paybak.navigation.id
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The app shell (app-architecture §6.1): tabs, the Add sheet, start screens and persistence. */
@RunWith(AndroidJUnit4::class)
class ShellTest {
    @get:Rule val compose = createEmptyComposeRule()

    private fun tag(tag: String) = compose.onNodeWithTag(tag)

    private fun awaitTag(tag: String, timeoutMillis: Long = 15_000) =
        compose.waitUntil(timeoutMillis) {
            compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()
        }

    private fun awaitGone(tag: String) =
        compose.waitUntil(10_000) {
            compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isEmpty()
        }

    @Test
    fun tabsSwitchFromEveryTab() {
        launchPaybak("homeActive").use {
            compose.awaitScreen("homeActive")
            for (from in Tab.entries) {
                tag("home.tab.${from.id}").performClick()
                for (to in Tab.entries) {
                    tag("home.tab.${to.id}").performClick()
                    awaitTag(rootOf(to))
                }
            }
        }
    }

    @Test
    fun theAddSheetOpensEveryModalFromEveryTab() {
        val modals =
            mapOf(
                "expense" to "addExpense",
                "payment" to "recordPayment",
                "lend" to "lendMoney",
                "group" to "newGroup",
            )
        launchPaybak("homeActive").use {
            compose.awaitScreen("homeActive")
            for (tab in Tab.entries) {
                tag("home.tab.${tab.id}").performClick()
                for ((row, modal) in modals) {
                    tag("home.tab.add").performClick()
                    awaitTag("home.addSheet.$row")
                    tag("home.addSheet.$row").performClick()
                    compose.awaitScreen(modal)
                    tag("$modal.header.close").performClick()
                    awaitGone("screen.$modal")
                    awaitTag(rootOf(tab))
                }
                tag("home.tab.add").performClick()
                awaitTag("home.addSheet.close")
                tag("home.addSheet.close").performClick()
                awaitGone("home.addSheet")
            }
        }
    }

    @Test
    fun systemBackClosesSheetsPushesAndModalsThenGoesHome() {
        launchPaybak("addExpenseCurrency").use { scenario ->
            awaitTag("screen.pickCurrency")
            scenario.pressSystemBack()
            compose.awaitScreen("addExpense")
            scenario.pressSystemBack()
            awaitGone("screen.addExpense")
            compose.awaitScreen("homeConfirmPayment")
            tag("home.tab.groups").performClick()
            awaitTag("screen.groups")
            scenario.pressSystemBack()
            compose.awaitScreen("homeConfirmPayment")
        }
    }

    @Test
    fun savingAModalShowsTheDetailWithAToast() {
        launchPaybak("addExpenseEmpty").use {
            compose.awaitScreen("addExpense")
            tag("addExpense.people").performClick()
            compose.awaitScreen("pickPeople")
            tag("pickPeople.done").performClick()
            compose.awaitScreen("addExpense")
            tag("addExpense.save").performClick()
            compose.awaitScreen("expense")
            compose.onNodeWithText("Expense added").assertExists()
            awaitGone("screen.addExpense")
        }
    }

    @Test
    fun creatingAGroupOpensItOnTheGroupsTab() {
        launchPaybak("newGroup").use {
            compose.awaitScreen("newGroup")
            tag("newGroup.create").performClick()
            compose.awaitScreen("group")
            compose.onNodeWithText("Group created").assertExists()
            assertTrue(paybakApp.ledger.ledger.value.groups.any { it.name == "New group" })
        }
    }

    @Test
    fun proFeaturesGoThroughThePaywall() {
        launchPaybak("profile").use {
            compose.awaitScreen("profile")
            tag("profile.export").performClick()
            compose.awaitScreen("paywall")
            tag("paywall.trial").performClick()
            compose.awaitScreen("privacyExport")
            assertTrue(paybakApp.ledger.snapshot.value.isPro)
        }
    }

    @Test
    fun aLinkOpensItsScreen() {
        launchPaybak("homeActive", link = "paybak://expense/e-goa-villa").use {
            compose.awaitScreen("expense")
            tag("screen.expense").assertExists()
        }
    }

    @Test
    fun dataSurvivesARelaunch() {
        launchPaybak("homeConfirmPayment").close()
        launchPaybak(resetOnboarding = false).use {
            compose.awaitScreen("homeConfirmPayment")
            compose.onNodeWithText("+₹2,900").assertExists()
        }
        assertEquals(1, paybakApp.ledger.snapshot.value.home.pendingClaims.size)
    }

    @Test
    fun theDebugMenuLoadsAnEmptyAccount() {
        launchPaybak("debugMenu").use {
            awaitTag("screen.debugMenu")
            compose.onNodeWithText("Start an empty account").performClick()
            compose.awaitScreen("homeFirstDay")
        }
    }

    @Test
    fun everyOnboardingIdStarts() {
        ScreenIds.onboarding.forEach { id -> launchPaybak(id).use { compose.awaitScreen(id) } }
    }

    @Test
    fun everyHomeAddRecordAndSettleIdStarts() =
        startAll(ScreenIds.scenarios.keys.filter { it in firstBatch })

    @Test
    fun everyOtherAppIdStarts() = startAll(ScreenIds.scenarios.keys.filter { it !in firstBatch })

    private val firstBatch: Set<String> by lazy {
        val all = ScreenIds.scenarios.keys.toList()
        all.take(all.size / 2).toSet()
    }

    /** Launches each id and waits for the root its scenario shows. */
    private fun startAll(ids: List<String>) {
        ids.forEach { id ->
            launchPaybak(id).use { awaitTag(expectedRoot(id, ScreenIds.scenarios.getValue(id))) }
        }
    }

    private fun expectedRoot(id: String, scenario: Scenario): String {
        homeRoots[id]?.let {
            return it
        }
        val top = scenario.sheet ?: scenario.stack.lastOrNull() ?: return rootOf(scenario.tab)
        return if (top == Route.AddSheet) "home.addSheet" else "screen.${top.id}"
    }

    /** A tab's root; Home's changes with its state, so its logo stands in. */
    private fun rootOf(tab: Tab): String =
        if (tab == Tab.Home) "home.logo" else "screen.${tab.route.id}"

    private companion object {
        /** Home's root carries its state: `screen.home<State>`. */
        val homeRoots =
            mapOf(
                "homeFirstDay" to "screen.homeFirstDay",
                "homeActive" to "screen.homeActive",
                "homeAllSettled" to "screen.homeAllSettled",
                "homeConfirmPayment" to "screen.homeConfirmPayment",
                "settlePaymentConfirmed" to "screen.homeActive",
            )
    }
}
