package agency.lacosta.venceya.domain

import java.time.LocalDate

/** Cada cuánto se repite. Un documento o una garantía normalmente no se repiten. */
enum class Repeat(val key: String) {
    NONE("none"),
    WEEKLY("weekly"),
    MONTHLY("monthly"),
    YEARLY("yearly");

    /** Suma [times] periodos a [start]. Mensual respeta el día original (31 → 30 → 31). */
    fun add(start: LocalDate, times: Long): LocalDate = when (this) {
        NONE -> start
        WEEKLY -> start.plusWeeks(times)
        MONTHLY -> start.plusMonths(times)
        YEARLY -> start.plusYears(times)
    }

    companion object {
        fun fromKey(key: String) = entries.firstOrNull { it.key == key } ?: NONE
    }
}
