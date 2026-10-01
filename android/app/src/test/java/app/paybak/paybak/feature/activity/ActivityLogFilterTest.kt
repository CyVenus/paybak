package app.paybak.paybak.feature.activity

import app.paybak.paybak.domain.Demo
import app.paybak.paybak.domain.calc.TimelineKind
import app.paybak.paybak.navigation.ActivityFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The `activityLog` filters on the demo. */
class ActivityLogFilterTest {
    private val demo = Demo.load()

    @Test
    fun aProjectLogsItsParts() {
        val filter = ActivityFilter.Project("pj-drone")
        val log = demo.logEvents(filter)
        assertTrue(log.isNotEmpty())
        assertTrue(log.all { it.groupId == "pj-drone" })
        assertTrue(log.any { it.kind == TimelineKind.ComponentChanged })
        assertEquals("Build a Drone", demo.logSubject(filter))
    }

    @Test
    fun aGroupLogsItsExpensesAndDrafts() {
        val titles = demo.logEvents(ActivityFilter.Group("g-flat302")).map { it.title }
        assertTrue("Meera added Electricity bill" in titles)
        assertTrue("Cooking gas draft created" in titles)
        assertTrue(titles.none { "Villa" in it })
    }

    @Test
    fun aGroupLogKeepsRemindersAboutItsExpenses() {
        val goa = demo.logEvents(ActivityFilter.Group("g-goa")).map { it.title }
        assertTrue("Kabir changed Villa (3 nights)" in goa)
        assertTrue("Dev added Fuel" in goa)
        assertTrue("Reminder sent to Rohan" !in goa)
    }

    @Test
    fun aPersonLogsWhatTheyAreOn() {
        val rohan = demo.logEvents(ActivityFilter.Person("p-rohan")).map { it.title }
        assertTrue("Reminder sent to Rohan" in rohan)
        assertTrue("Priya paid you" !in rohan)
        assertEquals("Rohan Verma", demo.logSubject(ActivityFilter.Person("p-rohan")))
    }

    @Test
    fun aCategoryLogsThatMonthOnly() {
        val filter = ActivityFilter.Category("food", "2026-09")
        val log = demo.logEvents(filter)
        val titles = log.map { it.title }
        assertTrue("You added Dinner at Olive Garden" in titles)
        assertTrue(titles.none { "Bastian" in it })
        assertTrue(log.all { demo.expense(it.ref)?.category == "food" })
        assertEquals("Food · September", demo.logSubject(filter))
    }
}
