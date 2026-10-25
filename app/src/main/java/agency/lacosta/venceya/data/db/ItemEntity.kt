package agency.lacosta.venceya.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import agency.lacosta.venceya.domain.Item
import agency.lacosta.venceya.domain.Kind
import agency.lacosta.venceya.domain.Repeat
import java.time.LocalDate

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val kind: String,
    /** Fecha como días desde 1970, sin hora: un vencimiento es un día, no un instante. */
    val firstDueEpochDay: Long,
    val repeat: String,
    /** Días antes de avisar, separados por coma: "1,0". */
    val remindDays: String,
    val amount: String,
    val note: String,
    val createdAt: Long,
)

fun ItemEntity.toDomain() = Item(
    id = id,
    name = name,
    kind = Kind.fromKey(kind),
    firstDue = LocalDate.ofEpochDay(firstDueEpochDay),
    repeat = Repeat.fromKey(repeat),
    remindDays = remindDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet(),
    amount = amount,
    note = note,
)

fun Item.toEntity(createdAt: Long = System.currentTimeMillis()) = ItemEntity(
    id = id,
    name = name,
    kind = kind.key,
    firstDueEpochDay = firstDue.toEpochDay(),
    repeat = repeat.key,
    remindDays = remindDays.sortedDescending().joinToString(","),
    amount = amount,
    note = note,
    createdAt = createdAt,
)
