package com.urdufonts.app.ui.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urdufonts.app.domain.models.PremiumProblem
import com.urdufonts.app.domain.models.toPremiumProblem
import com.urdufonts.app.domain.models.AttachmentDraft
import com.urdufonts.app.domain.models.AttachmentError
import com.urdufonts.app.domain.models.NewTicket
import com.urdufonts.app.domain.models.NewTicketError
import com.urdufonts.app.domain.models.TicketCategory
import com.urdufonts.app.domain.models.TicketLimits
import com.urdufonts.app.domain.usecases.GetUserSessionUseCase
import com.urdufonts.app.domain.usecases.CreateTicketUseCase
import com.urdufonts.app.domain.usecases.ValidateAttachmentUseCase
import com.urdufonts.app.domain.usecases.ValidateNewTicketUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewTicketUiState(
    val form: NewTicket = NewTicket(category = null, email = "", subject = "", description = "", attachment = null),
    /** Shown only after the first try to send, then kept up to date as the customer fixes things. */
    val errors: Set<NewTicketError> = emptySet(),
    val attachmentProblem: AttachmentProblem? = null,
    val submitting: Boolean = false
)

sealed interface NewTicketEvent {
    data class Created(val ticketId: Int) : NewTicketEvent
    data class Failed(val problem: PremiumProblem) : NewTicketEvent
}

class NewTicketViewModel(
    observeMember: GetUserSessionUseCase,
    private val validate: ValidateNewTicketUseCase,
    private val validateAttachment: ValidateAttachmentUseCase,
    private val createTicket: CreateTicketUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewTicketUiState())
    val uiState: StateFlow<NewTicketUiState> = _uiState.asStateFlow()

    private val _events = Channel<NewTicketEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var triedToSend = false

    init {
        // A signed-in customer's email is filled in for them; they can still change it
        viewModelScope.launch {
            val email = observeMember().first()?.email.orEmpty()
            if (email.isNotBlank()) edit { if (it.email.isBlank()) it.copy(email = email) else it }
        }
    }

    fun onCategory(category: TicketCategory) = edit { it.copy(category = category) }
    fun onEmail(value: String) = edit { it.copy(email = value.trim()) }
    fun onSubject(value: String) = edit { it.copy(subject = value.take(TicketLimits.SUBJECT_MAX)) }
    fun onDescription(value: String) = edit { it.copy(description = value.take(TicketLimits.DESCRIPTION_MAX)) }

    fun onAttachmentPicked(draft: AttachmentDraft) {
        val problem = when (validateAttachment(draft)) {
            AttachmentError.TOO_LARGE -> AttachmentProblem.TOO_LARGE
            AttachmentError.UNSUPPORTED_TYPE -> AttachmentProblem.UNSUPPORTED_TYPE
            AttachmentError.UNREADABLE -> AttachmentProblem.UNREADABLE
            null -> null
        }
        _uiState.update {
            it.copy(
                form = it.form.copy(attachment = if (problem == null) draft else it.form.attachment),
                attachmentProblem = problem
            )
        }
    }

    fun onAttachmentUnreadable() = _uiState.update { it.copy(attachmentProblem = AttachmentProblem.UNREADABLE) }

    fun onAttachmentRemoved() =
        _uiState.update { it.copy(form = it.form.copy(attachment = null), attachmentProblem = null) }

    fun submit() {
        val state = _uiState.value
        if (state.submitting) return
        triedToSend = true
        val errors = validate(state.form)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }
        _uiState.update { it.copy(errors = emptySet(), submitting = true) }
        viewModelScope.launch {
            createTicket(state.form).fold(
                onSuccess = { _events.send(NewTicketEvent.Created(it.id)) },
                onFailure = { _events.send(NewTicketEvent.Failed(it.toPremiumProblem())) }
            )
            _uiState.update { it.copy(submitting = false) }
        }
    }

    private fun edit(change: (NewTicket) -> NewTicket) = _uiState.update { state ->
        val form = change(state.form)
        state.copy(form = form, errors = if (triedToSend) validate(form) else emptySet())
    }
}
