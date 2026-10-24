package agency.lacosta.venceya.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import agency.lacosta.venceya.data.ItemRepository
import agency.lacosta.venceya.domain.Item
import agency.lacosta.venceya.domain.Summary
import agency.lacosta.venceya.domain.sortedByDue
import agency.lacosta.venceya.domain.summary
import agency.lacosta.venceya.ui.container
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeState(val today: LocalDate, val items: List<Item>, val summary: Summary)

class HomeViewModel(items: ItemRepository) : ViewModel() {

    val state: StateFlow<HomeState?> = items.items
        .map { list ->
            val today = LocalDate.now()
            HomeState(today, list.sortedByDue(today), list.summary(today))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    companion object {
        val Factory = viewModelFactory { initializer { HomeViewModel(container.items) } }
    }
}
