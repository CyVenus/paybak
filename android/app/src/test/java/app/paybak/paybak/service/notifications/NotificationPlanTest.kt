package app.paybak.paybak.service.notifications

import app.paybak.paybak.domain.model.InboxItem
import app.paybak.paybak.domain.model.InboxParams
import app.paybak.paybak.domain.model.InboxType
import app.paybak.paybak.domain.model.PushSettings
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

/** What gets posted and when the app wakes up (domain.md §10.1). */
class NotificationPlanTest {
    private val zone = ZoneId.of("Asia/Kolkata")
    private val nine = LocalTime.of(21, 0)

    private fun at(text: String): Instant = LocalDateTime.parse(text).atZone(zone).toInstant()

    private fun item(id: String, type: InboxType, created: String, read: Boolean = false) =
        InboxItem(id, type, at(created), read, InboxParams())

    @Test
    fun postsOnlyNewFreshUnreadItemsThatAreSwitchedOn() {
        val now = at("2026-09-30T21:05")
        val inbox =
            listOf(
                item("old", InboxType.PaymentReminder, "2026-09-30T21:00"),
                item("new", InboxType.PaymentReminder, "2026-09-30T21:00"),
                item("stale", InboxType.PaymentOverdue, "2026-09-30T09:00"),
                item("read", InboxType.PaymentConfirmed, "2026-09-30T21:04", read = true),
                item("muted", InboxType.MonthlySummary, "2026-09-30T21:01"),
            )
        val posted =
            NotificationPlan.toPost(
                inbox,
                seen = setOf("old"),
                now = now,
                push = PushSettings(monthlySummary = false),
            )
        assertEquals(listOf("new"), posted.map { it.id })
    }

    @Test
    fun wakesAtTheSchedulersNextMoment() {
        assertEquals(
            at("2026-09-30T20:00"),
            NotificationPlan.nextTick(at("2026-09-30T12:00"), zone, nine),
        )
        assertEquals(
            at("2026-09-30T21:00"),
            NotificationPlan.nextTick(at("2026-09-30T20:00"), zone, nine),
        )
        assertEquals(
            at("2026-10-01T09:00"),
            NotificationPlan.nextTick(at("2026-09-30T21:15"), zone, nine),
        )
    }

    @Test
    fun aReminderTimeBeforeNineMorningComesFirst() {
        val early = LocalTime.of(8, 30)
        assertEquals(
            at("2026-10-01T08:30"),
            NotificationPlan.nextTick(at("2026-09-30T21:15"), zone, early),
        )
    }

    @Test
    fun channelsFollowTheirKind() {
        assertEquals(
            NotificationChannelKind.Reminders,
            NotificationPlan.channel(InboxType.PaymentOverdue),
        )
        assertEquals(
            NotificationChannelKind.Summaries,
            NotificationPlan.channel(InboxType.MonthlySummary),
        )
        assertEquals(
            NotificationChannelKind.Activity,
            NotificationPlan.channel(InboxType.NewExpenseInGroup),
        )
        assertEquals(
            NotificationChannelKind.Payments,
            NotificationPlan.channel(InboxType.PaymentConfirmed),
        )
    }
}
