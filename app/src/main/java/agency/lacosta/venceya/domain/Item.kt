package agency.lacosta.venceya.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Algo que vence: una suscripción, un pago fijo, un documento, una garantía o un seguro.
 * [firstDue] es la primera fecha; si se repite, la próxima se calcula con [nextDue].
 */
data class Item(
    val id: Long = 0,
    val name: String,
    val kind: Kind,
    val firstDue: LocalDate,
    val repeat: Repeat = Repeat.NONE,
    /** Cuántos días antes avisar; puede ser más de uno. 0 = el mismo día. Vacío = sin avisos. */
    val remindDays: Set<Int> = setOf(1, 0),
    /** Monto opcional, como texto tal cual lo escribió la persona ("7,99 USD"). */
    val amount: String = "",
    val note: String = "",
) {
    /** La próxima fecha que vence a partir de [today]. Si no se repite y ya pasó, devuelve esa fecha pasada. */
    fun nextDue(today: LocalDate): LocalDate {
        if (repeat == Repeat.NONE || !firstDue.isBefore(today)) return firstDue
        // Salto aproximado y luego ajuste fino, para no iterar años de suscripción semana a semana.
        val unit = when (repeat) {
            Repeat.WEEKLY -> ChronoUnit.WEEKS
            Repeat.MONTHLY -> ChronoUnit.MONTHS
            else -> ChronoUnit.YEARS
        }
        var n = maxOf(0L, unit.between(firstDue, today) - 1)
        var due = repeat.add(firstDue, n)
        while (due.isBefore(today)) due = repeat.add(firstDue, ++n)
        return due
    }

    fun daysLeft(today: LocalDate): Long = ChronoUnit.DAYS.between(today, nextDue(today))

    fun urgency(today: LocalDate): Urgency = Urgency.of(daysLeft(today))

    /** Qué aviso toca hoy, si alguno. */
    fun reminderToday(today: LocalDate): Reminder? {
        val left = daysLeft(today)
        return when {
            left < 0 || left.toInt() !in remindDays -> null
            left == 0L -> Reminder.TODAY
            else -> Reminder.BEFORE
        }
    }
}

enum class Reminder { BEFORE, TODAY }

/** Qué tan cerca está; sirve para ordenar y para el color en pantalla. */
enum class Urgency {
    OVERDUE, THIS_WEEK, THIS_MONTH, LATER;

    companion object {
        fun of(daysLeft: Long) = when {
            daysLeft < 0 -> OVERDUE
            daysLeft <= 7 -> THIS_WEEK
            daysLeft <= 30 -> THIS_MONTH
            else -> LATER
        }
    }
}

/** Ordena por la próxima fecha; lo vencido primero. */
fun List<Item>.sortedByDue(today: LocalDate) = sortedWith(compareBy({ it.nextDue(today) }, { it.name.lowercase() }))
