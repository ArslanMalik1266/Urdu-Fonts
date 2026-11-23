package com.urdufonts.app.ui.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urdufonts.app.domain.models.PremiumProblem
import com.urdufonts.app.domain.models.toPremiumProblem
import com.urdufonts.app.domain.models.Ticket
import com.urdufonts.app.domain.usecases.ObserveTicketsUseCase
import com.urdufonts.app.domain.usecases.RefreshTicketsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The ticket list. [tickets] is null until the first answer: with [problem] set that is the error
 * state, without it the list is still loading. An empty list is the empty state.
 */
data class TicketListUiState(
    val tickets: List<Ticket>? = null,
    val problem: PremiumProblem? = null
) {
    val isLoading: Boolean get() = tickets == null && problem == null
}

class TicketListViewModel(
    observeTickets: ObserveTicketsUseCase,
    private val refreshTickets: RefreshTicketsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketListUiState())
    val uiState: StateFlow<TicketListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeTickets().collect { tickets -> _uiState.update { it.copy(tickets = tickets) } }
        }
        // Loaded once when the list opens. Coming back from a chat does not load it again by itself
        refresh()
    }

    /** Loads the list: when it opens, and for "Try again". */
    fun refresh() {
        _uiState.update { it.copy(problem = null) }
        viewModelScope.launch {
            refreshTickets().onFailure { error ->
                // A list already on screen stays; the problem is only shown when there is nothing else to show
                _uiState.update { it.copy(problem = error.toPremiumProblem()) }
            }
        }
    }
}
