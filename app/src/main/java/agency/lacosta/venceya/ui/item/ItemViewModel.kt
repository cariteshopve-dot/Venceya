package agency.lacosta.venceya.ui.item

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import agency.lacosta.venceya.data.ItemRepository
import agency.lacosta.venceya.data.settings.SettingsRepository
import agency.lacosta.venceya.domain.Item
import agency.lacosta.venceya.domain.Kind
import agency.lacosta.venceya.domain.Plan
import agency.lacosta.venceya.domain.Repeat
import agency.lacosta.venceya.ui.container
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ItemForm(
    val id: Long = 0,
    val name: String = "",
    val kind: Kind = Kind.SUBSCRIPTION,
    val due: LocalDate = LocalDate.now(),
    val repeat: Repeat = Kind.SUBSCRIPTION.defaultRepeat,
    val remindDays: Set<Int> = setOf(1, 0),
    val amount: String = "",
    val note: String = "",
    val loaded: Boolean = false,
    /** Nuevo y ya llegó al límite gratis. */
    val limitReached: Boolean = false,
) {
    val isNew get() = id == 0L
    val canSave get() = name.isNotBlank() && !limitReached
}

class ItemViewModel(
    savedState: SavedStateHandle,
    private val items: ItemRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val _form = MutableStateFlow(ItemForm())
    val form: StateFlow<ItemForm> = _form.asStateFlow()

    init {
        val id = savedState.get<Long>(ARG_ID) ?: 0L
        viewModelScope.launch {
            val existing = if (id != 0L) items.get(id) else null
            _form.value = if (existing != null) {
                ItemForm(
                    id = existing.id,
                    name = existing.name,
                    kind = existing.kind,
                    due = existing.firstDue,
                    repeat = existing.repeat,
                    remindDays = existing.remindDays,
                    amount = existing.amount,
                    note = existing.note,
                    loaded = true,
                )
            } else {
                val isPro = settings.settings.first().isPro
                ItemForm(loaded = true, limitReached = !Plan.canAdd(items.count(), isPro))
            }
        }
    }

    fun setName(v: String) = _form.update { it.copy(name = v) }
    fun setKind(v: Kind) = _form.update { it.copy(kind = v, repeat = if (it.isNew) v.defaultRepeat else it.repeat) }
    fun setDue(v: LocalDate) = _form.update { it.copy(due = v) }
    fun setRepeat(v: Repeat) = _form.update { it.copy(repeat = v) }
    /** Marca o desmarca un aviso; se pueden tener varios a la vez. */
    fun toggleRemind(v: Int) = _form.update { it.copy(remindDays = if (v in it.remindDays) it.remindDays - v else it.remindDays + v) }
    fun setAmount(v: String) = _form.update { it.copy(amount = v) }
    fun setNote(v: String) = _form.update { it.copy(note = v) }

    fun save(onDone: () -> Unit) {
        val f = _form.value
        if (!f.canSave) return
        viewModelScope.launch {
            items.save(
                Item(
                    id = f.id,
                    name = f.name.trim(),
                    kind = f.kind,
                    firstDue = f.due,
                    repeat = f.repeat,
                    remindDays = f.remindDays,
                    amount = f.amount.trim(),
                    note = f.note.trim(),
                ),
            )
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        val id = _form.value.id
        if (id == 0L) return
        viewModelScope.launch {
            items.delete(id)
            onDone()
        }
    }

    companion object {
        const val ARG_ID = "id"
        val REMIND_OPTIONS = listOf(0, 1, 3, 7, 15, 30)

        val Factory = viewModelFactory {
            initializer { ItemViewModel(createSavedStateHandle(), container.items, container.settings) }
        }
    }
}
