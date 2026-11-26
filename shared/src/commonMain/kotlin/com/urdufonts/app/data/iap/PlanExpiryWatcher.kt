package com.urdufonts.app.data.iap

import com.urdufonts.app.platform.currentTimeMillis
import kotlinx.coroutines.IO
import kotlin.concurrent.Volatile

import com.urdufonts.app.platform.AppLog as Log
import com.urdufonts.app.data.local.UserPreferences
import com.urdufonts.app.domain.models.UserEntitlements
import com.urdufonts.app.domain.repo.EntitlementRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Instant

/**
 * When the plan's time from the entitlement API runs out, asks the server again at
 * 10s, 20s and 40s after that time to see whether the plan renewed or changed.
 * The server's answer is always trusted: a renewal brings a new time and the watch starts
 * over; an ended plan comes back unlocked = false. If it still says premium, the next app
 * start fixes it.
 */
class PlanExpiryWatcher(
    private val entitlementRepository: EntitlementRepository,
    private val userPreferences: UserPreferences
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null
    @Volatile private var currentTarget: Long? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    fun start() {
        if (job != null) return
        // Ask once at start: a plan that renewed (or ended) while the app was closed shows its new state
        scope.launch { refresh(force = true) }
        job = scope.launch {
            entitlementRepository.entitlementsFlow
                .map { nextChangeTime(it) }
                // The same time again (an unchanged answer) must not restart the checks
                .distinctUntilChanged()
                .collectLatest { target ->
                    currentTarget = target
                    if (target != null) runChecks(target)
                }
        }
    }

    /** The app came back to the front: a time that passed meanwhile gets one check. */
    fun onAppResumed() {
        // Always ask (at most once a minute, the repository's cooldown): a plan that already looks ended may
        // have renewed on the server since, and with no time left to watch nothing else would ask again
        val target = currentTarget
        scope.launch { refresh(force = target != null && currentTimeMillis() >= target) }
    }

    private suspend fun runChecks(target: Long) {
        val checkTimes = CHECK_OFFSETS_MS.map { target + it }
        val now = currentTimeMillis()
        if (now >= checkTimes.last()) {
            // All checks are already late (app was closed): ask once now
            refresh(force = true)
            return
        }
        for (checkAt in checkTimes) {
            val wait = checkAt - currentTimeMillis()
            if (wait < 0) continue
            delay(wait)
            Log.d(TAG, "Plan time passed, checking for renewal (${(checkAt - target) / 1000}s after)")
            // A renewed or changed plan brings a new time, which cancels this loop and starts over
            refresh(force = true)
        }
    }

    private suspend fun refresh(force: Boolean) {
        val userId = userPreferences.userSession.firstOrNull()?.email?.takeIf { it.isNotBlank() }
            ?: entitlementRepository.entitlementsFlow.firstOrNull()?.externalUserId?.takeIf { it.isNotBlank() }
            ?: return
        entitlementRepository.refreshEntitlements(userId, force)
    }

    /** The earliest time at which the plan ends or a scheduled change takes effect. */
    private fun nextChangeTime(e: UserEntitlements): Long? {
        val times = e.entitlements.filter { it.granted }.mapNotNull { parseTime(it.expiresAt) } +
            listOfNotNull(parseTime(e.pendingChange?.effectiveAt))
        return times.minOrNull()
    }

    companion object {
        private const val TAG = "PlanExpiryWatcher"
        // The store tells the backend about a renewal a little after the old time ends (often over a minute in
        // the App Store sandbox), so keep asking for a few minutes
        private val CHECK_OFFSETS_MS = listOf(10_000L, 30_000L, 60_000L, 120_000L, 240_000L)

        /** Reads ISO-8601 times like 2026-11-16T10:00:00Z, with or without fractions or an offset. */
        internal fun parseTime(raw: String?): Long? {
            if (raw.isNullOrBlank()) return null
            var s = raw.trim().replace(' ', 'T').replace(Regex("\\.\\d+"), "")
            if (s.endsWith("Z", ignoreCase = true)) s = s.dropLast(1) + "+00:00"
            val hasZone = Regex("[+-]\\d{2}:?\\d{2}$").containsMatchIn(s.substringAfter('T', ""))
            // A zone without a colon (+0500) gets one; no zone at all means UTC
            s = if (hasZone) s.replace(Regex("([+-]\\d{2})(\\d{2})$"), "$1:$2") else "${s}Z"
            return try {
                Instant.parse(s).toEpochMilliseconds()
            } catch (e: Exception) {
                Log.w(TAG, "Could not read plan time: $raw")
                null
            }
        }
    }
}
