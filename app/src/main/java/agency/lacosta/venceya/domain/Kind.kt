package agency.lacosta.venceya.domain

/** Qué tipo de cosa vence. Los papeles del carro van en la app del carro, no aquí. */
enum class Kind(val key: String) {
    SUBSCRIPTION("subscription"),
    BILL("bill"),
    DOCUMENT("document"),
    WARRANTY("warranty"),
    INSURANCE("insurance");

    companion object {
        fun fromKey(key: String) = entries.firstOrNull { it.key == key } ?: BILL
    }
}
