package agency.lacosta.venceya.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ItemTest {
    private val today = LocalDate.of(2026, 10, 24)

    private fun item(due: LocalDate, repeat: Repeat = Repeat.NONE, before: Int = 3) =
        Item(name = "x", kind = Kind.BILL, firstDue = due, repeat = repeat, remindDaysBefore = before)

    @Test fun oneTimeInFuture() {
        val i = item(LocalDate.of(2026, 12, 5))
        assertEquals(42, i.daysLeft(today))
        assertEquals(Urgency.LATER, i.urgency(today))
    }

    @Test fun oneTimePastIsOverdue() {
        val i = item(LocalDate.of(2026, 10, 20))
        assertEquals(-4, i.daysLeft(today))
        assertEquals(Urgency.OVERDUE, i.urgency(today))
    }

    @Test fun monthlyRollsForward() {
        val i = item(LocalDate.of(2025, 1, 27), Repeat.MONTHLY)
        assertEquals(LocalDate.of(2026, 10, 27), i.nextDue(today))
        assertEquals(Urgency.THIS_WEEK, i.urgency(today))
    }

    @Test fun monthlyDueTodayStaysToday() {
        val i = item(LocalDate.of(2026, 1, 24), Repeat.MONTHLY)
        assertEquals(today, i.nextDue(today))
    }

    @Test fun monthlyKeepsDay31() {
        val i = item(LocalDate.of(2026, 1, 31), Repeat.MONTHLY)
        assertEquals(LocalDate.of(2026, 10, 31), i.nextDue(today))
        assertEquals(LocalDate.of(2026, 11, 30), i.nextDue(LocalDate.of(2026, 11, 1)))
    }

    @Test fun weeklyAndYearly() {
        assertEquals(LocalDate.of(2026, 10, 30), item(LocalDate.of(2024, 1, 5), Repeat.WEEKLY).nextDue(today))
        assertEquals(LocalDate.of(2027, 3, 14), item(LocalDate.of(2020, 3, 14), Repeat.YEARLY).nextDue(today))
        assertEquals(LocalDate.of(2027, 2, 28), item(LocalDate.of(2024, 2, 29), Repeat.YEARLY).nextDue(today))
    }

    @Test fun reminders() {
        assertEquals(Reminder.BEFORE, item(LocalDate.of(2026, 10, 27)).reminderToday(today))
        assertEquals(Reminder.TODAY, item(today).reminderToday(today))
        assertNull(item(LocalDate.of(2026, 10, 26)).reminderToday(today))
        assertEquals(Reminder.TODAY, item(today, before = 0).reminderToday(today))
    }

    @Test fun sortsByNextDue() {
        val a = item(LocalDate.of(2026, 12, 5)).copy(name = "Pasaporte")
        val b = item(LocalDate.of(2025, 1, 27), Repeat.MONTHLY).copy(name = "Netflix")
        val c = item(LocalDate.of(2026, 10, 20)).copy(name = "Luz")
        assertEquals(listOf("Luz", "Netflix", "Pasaporte"), listOf(a, b, c).sortedByDue(today).map { it.name })
    }

    @Test fun freeLimit() {
        assertTrue(Plan.canAdd(4, isPro = false))
        assertFalse(Plan.canAdd(5, isPro = false))
        assertTrue(Plan.canAdd(50, isPro = true))
    }
}
