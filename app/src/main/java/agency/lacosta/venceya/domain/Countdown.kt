package agency.lacosta.venceya.domain

/** Lo que muestra el anillo de cuenta regresiva de cada vencimiento. */
object Countdown {

    enum class Span { DAYS, MONTHS, YEARS }

    /** Texto corto dentro del anillo: 3 días, 4 meses, 2 años. Lo vencido cuenta días hacia atrás. */
    data class Short(val value: Long, val span: Span)

    /** Cuántos días cubre el anillo completo: un periodo si se repite, 60 días si no. */
    private fun window(repeat: Repeat) = when (repeat) {
        Repeat.WEEKLY -> 7
        Repeat.MONTHLY -> 30
        Repeat.YEARLY -> 365
        Repeat.NONE -> 60
    }

    /** Qué tan lleno está el anillo, de 0 a 1: se llena a medida que se acerca la fecha. */
    fun fraction(daysLeft: Long, repeat: Repeat): Float {
        if (daysLeft <= 0) return 1f
        return (1f - daysLeft.toFloat() / window(repeat)).coerceIn(0.04f, 1f)
    }

    fun short(daysLeft: Long): Short {
        val d = kotlin.math.abs(daysLeft)
        return when {
            d < 100 -> Short(d, Span.DAYS)
            d < 730 -> Short(d / 30, Span.MONTHS)
            else -> Short(d / 365, Span.YEARS)
        }
    }
}

/** Cuántos hay en cada franja, para el resumen de arriba. */
data class Summary(val thisWeek: Int, val thisMonth: Int, val later: Int)

fun List<Item>.summary(today: java.time.LocalDate): Summary {
    val byUrgency = groupingBy { it.urgency(today) }.eachCount()
    return Summary(
        thisWeek = (byUrgency[Urgency.OVERDUE] ?: 0) + (byUrgency[Urgency.THIS_WEEK] ?: 0),
        thisMonth = byUrgency[Urgency.THIS_MONTH] ?: 0,
        later = byUrgency[Urgency.LATER] ?: 0,
    )
}
