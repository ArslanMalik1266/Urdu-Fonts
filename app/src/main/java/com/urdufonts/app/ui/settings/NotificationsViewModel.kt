package com.urdufonts.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urdufonts.app.domain.usecases.ObserveNotificationsEnabledUseCase
import com.urdufonts.app.domain.usecases.SetNotificationsEnabledUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The Notifications switch in Settings. The switch shows on only while the phone allows them too. */
class NotificationsViewModel(
    observeNotifications: ObserveNotificationsEnabledUseCase,
    private val setNotificationsEnabled: SetNotificationsEnabledUseCase,
) : ViewModel() {

    /** The app's own on / off choice. */
    val enabled: StateFlow<Boolean> = observeNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    private val _offFailed = Channel<Unit>(Channel.BUFFERED)
    /** Turning off could not reach the backend, so the switch stayed on. */
    val offFailed: Flow<Unit> = _offFailed.receiveAsFlow()

    /** Off tells the backend to stop sending to this phone; when that call fails the switch stays on. */
    fun onNotifications(on: Boolean) {
        viewModelScope.launch {
            setNotificationsEnabled(on).onFailure { if (!on) _offFailed.send(Unit) }
        }
    }
}
