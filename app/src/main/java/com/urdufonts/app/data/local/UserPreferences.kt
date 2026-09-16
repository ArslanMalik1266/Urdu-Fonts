package com.urdufonts.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull

import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.urdufonts.app.domain.models.UserSession

// Define the DataStore delegate at the top of the file
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
        private val USER_TOKEN_KEY = stringPreferencesKey("user_token")
        private val USER_ID_KEY = intPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_AVATAR_KEY = stringPreferencesKey("user_avatar")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val IS_PRO_USER_KEY = booleanPreferencesKey("is_pro_user")
        private val SUBSCRIPTION_PLAN_KEY = stringPreferencesKey("subscription_plan")
        private val SUBSCRIPTION_PURCHASE_TOKEN_KEY = stringPreferencesKey("subscription_purchase_token")
        private val DOWNLOAD_CYCLE_START_TIMESTAMP_KEY = longPreferencesKey("download_cycle_start_timestamp")
        private val DOWNLOADED_FONT_IDS_KEY = androidx.datastore.preferences.core.stringSetPreferencesKey("downloaded_font_ids")
        private const val TWENTY_FOUR_HOURS_MS = 24 * 60 * 60 * 1000L

        fun getAnonymousDeviceId(context: Context): String {
            val androidId = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            ) ?: "unknown_device"
            val hashBytes = java.security.MessageDigest.getInstance("SHA-256")
                .digest(androidId.toByteArray(Charsets.UTF_8))
            val sha256Hex = hashBytes.joinToString("") { "%02x".format(it) }
            return "anon_android_$sha256Hex"
        }
    }

    // Read set of unique font IDs downloaded in the current 24-hour cycle (resets 24h after 1st download)
    val downloadedFontIds: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            val startTimestamp = preferences[DOWNLOAD_CYCLE_START_TIMESTAMP_KEY] ?: 0L
            val now = System.currentTimeMillis()
            if (now - startTimestamp < TWENTY_FOUR_HOURS_MS) {
                preferences[DOWNLOADED_FONT_IDS_KEY] ?: emptySet()
            } else {
                emptySet()
            }
        }

    // Read download count of unique fonts downloaded in the active 24-hour window
    val downloadCount: Flow<Int> = downloadedFontIds.map { it.size }

    // Record a font download. Starts 24-hour cycle on first download; resets after 24h.
    suspend fun recordFontDownloaded(fontId: String): Boolean {
        var isNewUnique = false
        val now = System.currentTimeMillis()
        context.dataStore.edit { preferences ->
            val startTimestamp = preferences[DOWNLOAD_CYCLE_START_TIMESTAMP_KEY] ?: 0L
            val isExpired = (now - startTimestamp >= TWENTY_FOUR_HOURS_MS)

            val currentSet = if (!isExpired) {
                preferences[DOWNLOADED_FONT_IDS_KEY]?.toMutableSet() ?: mutableSetOf()
            } else {
                mutableSetOf()
            }

            if (!currentSet.contains(fontId)) {
                if (currentSet.isEmpty() || isExpired) {
                    preferences[DOWNLOAD_CYCLE_START_TIMESTAMP_KEY] = now
                }
                currentSet.add(fontId)
                preferences[DOWNLOADED_FONT_IDS_KEY] = currentSet
                isNewUnique = true
            }
        }
        return isNewUnique
    }

    // Check if a font was already downloaded in the active 24-hour window
    suspend fun isFontDownloadedToday(fontId: String): Boolean {
        val now = System.currentTimeMillis()
        val prefs = context.dataStore.data.firstOrNull() ?: return false
        val startTimestamp = prefs[DOWNLOAD_CYCLE_START_TIMESTAMP_KEY] ?: 0L
        if (now - startTimestamp >= TWENTY_FOUR_HOURS_MS) return false
        val set = prefs[DOWNLOADED_FONT_IDS_KEY] ?: return false
        return set.contains(fontId)
    }

    // Read subscription pro status (returns a Flow)
    val isProUser: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_PRO_USER_KEY] == true
        }

    // Read subscription plan ID (returns a Flow)
    val subscriptionPlan: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[SUBSCRIPTION_PLAN_KEY]
        }

    // Read subscription purchase token (returns a Flow)
    val subscriptionPurchaseToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[SUBSCRIPTION_PURCHASE_TOKEN_KEY]
        }

    // Save subscription status to DataStore
    suspend fun saveSubscriptionStatus(isPro: Boolean, planId: String? = null, purchaseToken: String? = null) {
        context.dataStore.edit { preferences ->
            preferences[IS_PRO_USER_KEY] = isPro
            if (planId != null) preferences[SUBSCRIPTION_PLAN_KEY] = planId else preferences.remove(SUBSCRIPTION_PLAN_KEY)
            if (purchaseToken != null) preferences[SUBSCRIPTION_PURCHASE_TOKEN_KEY] = purchaseToken else preferences.remove(SUBSCRIPTION_PURCHASE_TOKEN_KEY)
        }
    }

    // Read the onboarding completed status (returns a Flow)
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] == true
        }

    // Save the onboarding completed status
    suspend fun saveOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = completed
        }
    }

    // Read the active user session reactively
    val userSession: Flow<UserSession?> = context.dataStore.data
        .map { preferences ->
            val token = preferences[USER_TOKEN_KEY]
            if (token != null) {
                UserSession(
                    token = token,
                    id = preferences[USER_ID_KEY] ?: 0,
                    name = preferences[USER_NAME_KEY] ?: "",
                    email = preferences[USER_EMAIL_KEY] ?: "",
                    avatar = preferences[USER_AVATAR_KEY],
                    role = preferences[USER_ROLE_KEY] ?: "customer"
                )
            } else {
                null
            }
        }

    // Save the user session to DataStore preferences
    suspend fun saveUserSession(session: UserSession) {
        context.dataStore.edit { preferences ->
            preferences[USER_TOKEN_KEY] = session.token
            preferences[USER_ID_KEY] = session.id
            preferences[USER_NAME_KEY] = session.name
            preferences[USER_EMAIL_KEY] = session.email
            session.avatar?.let { preferences[USER_AVATAR_KEY] = it } ?: preferences.remove(USER_AVATAR_KEY)
            preferences[USER_ROLE_KEY] = session.role
        }
    }

    // Clear the user session from DataStore (logout)
    suspend fun clearUserSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_NAME_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(USER_AVATAR_KEY)
            preferences.remove(USER_ROLE_KEY)
        }
    }
}