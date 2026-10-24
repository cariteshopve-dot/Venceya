package agency.lacosta.venceya.ui.item

import androidx.annotation.StringRes
import agency.lacosta.venceya.R
import agency.lacosta.venceya.domain.Kind
import agency.lacosta.venceya.domain.Repeat

@get:StringRes
val Kind.labelRes: Int
    get() = when (this) {
        Kind.SUBSCRIPTION -> R.string.kind_subscription
        Kind.BILL -> R.string.kind_bill
        Kind.DOCUMENT -> R.string.kind_document
        Kind.WARRANTY -> R.string.kind_warranty
        Kind.INSURANCE -> R.string.kind_insurance
    }

@get:StringRes
val Repeat.labelRes: Int
    get() = when (this) {
        Repeat.NONE -> R.string.repeat_none
        Repeat.WEEKLY -> R.string.repeat_weekly
        Repeat.MONTHLY -> R.string.repeat_monthly
        Repeat.YEARLY -> R.string.repeat_yearly
    }

/** Lo que se repite por defecto según el tipo, para que agregar sea más rápido. */
val Kind.defaultRepeat: Repeat
    get() = when (this) {
        Kind.SUBSCRIPTION, Kind.BILL -> Repeat.MONTHLY
        Kind.INSURANCE -> Repeat.YEARLY
        Kind.DOCUMENT, Kind.WARRANTY -> Repeat.NONE
    }
