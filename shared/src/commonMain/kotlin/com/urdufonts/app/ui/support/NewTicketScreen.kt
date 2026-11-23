package com.urdufonts.app.ui.support

import com.urdufonts.app.resources.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import com.urdufonts.app.ui.support.kit.LocalAppSnackbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.urdufonts.app.domain.models.NewTicketError
import com.urdufonts.app.domain.models.TicketCategory
import com.urdufonts.app.domain.models.TicketLimits
import com.urdufonts.app.ui.support.kit.AppChip
import com.urdufonts.app.ui.support.kit.AppTopBar
import com.urdufonts.app.ui.support.kit.PrimaryButton
import com.urdufonts.app.ui.support.kit.Dimens
import com.urdufonts.app.ui.support.kit.springOverscroll
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The form that starts a conversation: what it is about, an email to reply to, a subject, what happened and an
 * optional file. Errors show only after the first try to send, then follow the customer's fixes.
 */
@Composable
fun NewTicketScreen(onBack: () -> Unit, onCreated: (ticketId: Int) -> Unit, viewModel: NewTicketViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // The app's one snackbar, above the screen
    val snackbarHostState = LocalAppSnackbar.current
    val pickers = rememberAttachmentPickers(onPicked = viewModel::onAttachmentPicked, onFailed = viewModel::onAttachmentUnreadable)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is NewTicketEvent.Created -> onCreated(event.ticketId)
                is NewTicketEvent.Failed -> snackbarHostState.showSnackbar(org.jetbrains.compose.resources.getString(event.problem.text))
            }
        }
    }
    val focusManager = LocalFocusManager.current
    val form = state.form
    val errors = state.errors
    Scaffold(
        // The whole screen sits above the keyboard, so the field being typed in is never under it
        modifier = Modifier.imePadding(),
        topBar = { AppTopBar(title = stringResource(Res.string.ticket_new_title), onBack = onBack) },
        // Pinned, so the form never ends in empty space and Send is always in reach
        bottomBar = {
            PrimaryButton(
                text = stringResource(Res.string.ticket_submit),
                enabled = true,
                loading = state.submitting,
                onClick = viewModel::submit,
                modifier = Modifier.navigationBarsPadding().padding(Dimens.Space16).fillMaxWidth(),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.Space16),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                // A tap anywhere that is not a field puts the keyboard away (a field keeps its own taps)
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
                .clipToBounds()
                .springOverscroll()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.Space16, vertical = Dimens.Space8),
        ) {
            Column {
                FieldLabel(stringResource(Res.string.ticket_topic_label))
                CategoryChips(selected = form.category, onSelect = viewModel::onCategory)
                if (NewTicketError.CATEGORY_MISSING in errors) FieldError(stringResource(Res.string.ticket_error_category))
            }
            Column {
                FieldLabel(stringResource(Res.string.ticket_email_label))
                SupportField(
                    value = form.email,
                    onValueChange = viewModel::onEmail,
                    hint = stringResource(Res.string.ticket_email_hint),
                    keyboardType = KeyboardType.Email,
                    capitalization = KeyboardCapitalization.None,
                    error = when {
                        NewTicketError.EMAIL_MISSING in errors -> stringResource(Res.string.ticket_error_email_missing)
                        NewTicketError.EMAIL_INVALID in errors -> stringResource(Res.string.ticket_error_email_invalid)
                        else -> null
                    },
                )
            }
            Column {
                FieldLabel(stringResource(Res.string.ticket_subject_label), counter = "${form.subject.length}/${TicketLimits.SUBJECT_MAX}")
                SupportField(
                    value = form.subject,
                    onValueChange = viewModel::onSubject,
                    hint = stringResource(Res.string.ticket_subject_hint),
                    error = stringResource(Res.string.ticket_error_subject).takeIf { NewTicketError.SUBJECT_MISSING in errors },
                )
            }
            Column {
                FieldLabel(stringResource(Res.string.ticket_description_label), counter = "${form.description.length}/${TicketLimits.DESCRIPTION_MAX}")
                SupportField(
                    value = form.description,
                    onValueChange = viewModel::onDescription,
                    hint = stringResource(Res.string.ticket_description_hint),
                    singleLine = false,
                    minHeight = Dimens.TicketDescriptionMinHeight,
                    error = stringResource(Res.string.ticket_error_description).takeIf { NewTicketError.DESCRIPTION_MISSING in errors },
                )
            }
            AttachmentBox(
                attachment = form.attachment,
                problem = state.attachmentProblem,
                onGallery = pickers.gallery,
                onFiles = pickers.files,
                onRemove = viewModel::onAttachmentRemoved,
            )
        }
    }
}

/** The topics a ticket can be about, as the app's chips wrapping onto as many rows as they need. One is picked. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryChips(selected: TicketCategory?, onSelect: (TicketCategory) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space8),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space8),
        modifier = Modifier.fillMaxWidth(),
    ) {
        TicketCategory.entries.forEach { category ->
            AppChip(text = stringResource(category.label), selected = category == selected, onClick = { onSelect(category) })
        }
    }
}
