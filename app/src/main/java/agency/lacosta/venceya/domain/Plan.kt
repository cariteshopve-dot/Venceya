package agency.lacosta.venceya.domain

/** Lo que permite la versión gratis. El pago único (2,99 USD) quita el límite. */
object Plan {
    const val FREE_LIMIT = 5

    fun canAdd(currentCount: Int, isPro: Boolean) = isPro || currentCount < FREE_LIMIT
}
