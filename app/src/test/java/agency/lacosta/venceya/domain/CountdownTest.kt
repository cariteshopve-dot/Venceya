package agency.lacosta.venceya.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class CountdownTest {

    @Test fun fractionFillsAsDateNears() {
        assertEquals(1f, Countdown.fraction(0, Repeat.MONTHLY))
        assertEquals(1f, Countdown.fraction(-3, Repeat.NONE))
        assertEquals(0.9f, Countdown.fraction(3, Repeat.MONTHLY), 0.001f)
        assertEquals(0.04f, Countdown.fraction(400, Repeat.NONE))
    }

    @Test fun shortLabels() {
        assertEquals(Countdown.Short(3, Countdown.Span.DAYS), Countdown.short(3))
        assertEquals(Countdown.Short(4, Countdown.Span.DAYS), Countdown.short(-4))
        assertEquals(Countdown.Short(4, Countdown.Span.MONTHS), Countdown.short(141))
        assertEquals(Countdown.Short(2, Countdown.Span.YEARS), Countdown.short(800))
    }

    @Test fun summaryCountsOverdueAsThisWeek() {
        val today = LocalDate.of(2026, 10, 24)
        fun at(d: LocalDate) = Item(name = "x", kind = Kind.BILL, firstDue = d)
        val list = listOf(at(today.minusDays(2)), at(today.plusDays(3)), at(today.plusDays(20)), at(today.plusDays(90)))
        assertEquals(Summary(thisWeek = 2, thisMonth = 1, later = 1), list.summary(today))
    }
}
