package app.paybak.paybak.navigation

import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentMethod
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MainNavigatorTest {
    private fun MainNavigator.routes() = entries.map { it.route }

    @Test
    fun pushesModalsAndSheetsStackOverTheTabs() {
        val navigator = MainNavigator()
        navigator.open(Route.Group("g-goa"))
        navigator.open(Route.AddExpense())
        navigator.open(Route.PickPeople(PickRequest("r")))
        navigator.open(Route.PickCurrency(PickRequest("c")))
        assertEquals(Route.PickPeople(PickRequest("r")), navigator.screen.route)
        assertEquals(Route.PickCurrency(PickRequest("c")), navigator.sheet?.route)
        // A second sheet replaces the first.
        navigator.open(Route.PickDate(PickRequest("d")))
        assertEquals(5, navigator.entries.size)
    }

    @Test
    fun backClosesTheSheetThenPopsThenDismissesThenGoesHome() {
        val navigator = MainNavigator(MainState(selectedTab = Tab.Groups))
        navigator.open(Route.Group("g-goa"))
        navigator.open(Route.RecordPayment())
        navigator.open(Route.PickGroup(PickRequest("g")))
        assertTrue(navigator.back())
        assertNull(navigator.sheet)
        assertTrue(navigator.back())
        assertEquals(Route.Group("g-goa"), navigator.screen.route)
        assertTrue(navigator.back())
        assertEquals(Route.Tabs, navigator.screen.route)
        assertTrue(navigator.back())
        assertEquals(Tab.Home, navigator.selectedTab)
        assertFalse(navigator.handlesBack)
        assertFalse(navigator.back())
    }

    @Test
    fun dismissModalClosesItsWholeLayer() {
        val navigator = MainNavigator()
        navigator.open(Route.Friend("p-rohan"))
        navigator.open(Route.AddExpense())
        navigator.open(Route.PickPeople(PickRequest("r")))
        navigator.dismissModal()
        assertEquals(listOf(Route.Tabs, Route.Friend("p-rohan")), navigator.routes())
    }

    @Test
    fun didSavePutsTheDetailUnderTheModalAndToasts() {
        val navigator = MainNavigator()
        navigator.open(Route.AddExpense())
        navigator.open(Route.PickPeople(PickRequest("r")))
        navigator.didSave(Route.Expense("e-1"), "Expense added")
        assertEquals(listOf(Route.Tabs, Route.Expense("e-1")), navigator.routes())
    }

    @Test
    fun didCreateGroupShowsItOnTheGroupsTab() {
        val navigator =
            MainNavigator(
                MainState(selectedTab = Tab.Activity, groupsSegment = GroupsSegment.Friends)
            )
        navigator.open(Route.NewGroup(NewGroupMode.Project))
        navigator.didCreateGroup("pj-new", isProject = true)
        assertEquals(Tab.Groups, navigator.selectedTab)
        assertEquals(GroupsSegment.Groups, navigator.groupsSegment)
        assertEquals(listOf(Route.Tabs, Route.Project("pj-new")), navigator.routes())
    }

    @Test
    fun requireProGoesThroughThePaywallOnTheFreePlan() {
        var pro = false
        val navigator = MainNavigator(isPro = { pro })
        navigator.open(Route.PrivacyData)
        navigator.requirePro(Route.PrivacyExport)
        assertEquals(Route.Paywall(continueTo = Route.PrivacyExport), navigator.screen.route)
        pro = true
        navigator.finishPaywall()
        assertEquals(listOf(Route.Tabs, Route.PrivacyData, Route.PrivacyExport), navigator.routes())
        navigator.requirePro(Route.Ask)
        assertEquals(Route.Ask, navigator.screen.route)
    }

    @Test
    fun aPickerAnswersThroughTheResultChannel() {
        val navigator = MainNavigator()
        navigator.open(Route.AddExpense())
        navigator.open(Route.PickPeople(PickRequest("r")))
        navigator.complete("r", RouteResult.People(listOf("p-priya")))
        assertEquals(Route.AddExpense(), navigator.screen.route)
        assertEquals(RouteResult.People(listOf("p-priya")), navigator.results["r"])
        navigator.open(Route.ScanReceipt(PickRequest("s")))
        navigator.complete("s", RouteResult.Currency("INR"))
        assertEquals(Route.AddExpense(), navigator.screen.route)
    }

    @Test
    fun theStateSurvivesProcessDeath() {
        val navigator =
            MainNavigator(MainState(selectedTab = Tab.Groups, insightsMonth = "2026-09"))
        navigator.open(Route.Group("g-goa"))
        navigator.open(Route.RecordPayment(RecordPaymentArgs(toId = "p-kabir", amount = 140_000)))
        navigator.open(Route.Paywall(continueTo = Route.Recurring("g-goa")))
        val restored = MainNavigator(MainState.decode(navigator.state.encode())!!)
        assertEquals(navigator.routes(), restored.routes())
        assertEquals(navigator.entries.map { it.key }, restored.entries.map { it.key })
        assertEquals(Tab.Groups, restored.selectedTab)
        assertEquals(YearMonth.of(2026, 9), restored.insightsMonth)
    }

    @Test
    fun deepLinksOpenTheirScreens() {
        val navigator = MainNavigator()
        navigator.open(
            DeepLink.parse(
                "paybak://record-payment?to=p-kabir&amount=140000&context=group:g-goa&method=upi"
            )!!
        )
        assertEquals(
            Route.RecordPayment(
                RecordPaymentArgs(
                    fromId = ME,
                    toId = "p-kabir",
                    amount = 140_000,
                    method = PaymentMethod.Upi,
                    groupId = "g-goa",
                )
            ),
            navigator.screen.route,
        )
        navigator.open(DeepLink.parse("paybak://expense/e-goa-villa")!!)
        assertEquals(listOf(Route.Tabs, Route.Expense("e-goa-villa")), navigator.routes())
        assertEquals(Tab.Activity, navigator.selectedTab)
        navigator.open(DeepLink.parse("paybak://insights?month=2026-09")!!)
        assertEquals(ActivitySegment.Insights, navigator.activitySegment)
        navigator.open(
            DeepLink.parse("paybak://activity?claim=pay-esha-olive&action=notReceived")!!
        )
        assertEquals(Route.NotReceived("pay-esha-olive"), navigator.sheet?.route)
        assertEquals(DeepLink.Remind("p-rohan"), DeepLink.parse("paybak://remind?person=p-rohan"))
        assertEquals(
            DeepLink.RecurringDraft("d-gas-09"),
            DeepLink.parse("paybak://recurring-draft/d-gas-09"),
        )
        assertNull(DeepLink.parse("https://paybak.app/i/arjun"))
    }
}
