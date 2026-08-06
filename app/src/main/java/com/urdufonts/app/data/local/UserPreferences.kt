package com.urdufonts.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
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
        private val DOWNLOAD_COUNT_KEY = intPreferencesKey("download_count")
    }

    // Read download count (returns a Flow)
    val downloadCount: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[DOWNLOAD_COUNT_KEY] ?: 0
        }

    // Increment download count and return new value
    suspend fun incrementDownloadCount(): Int {
        var updated = 0
        context.dataStore.edit { preferences ->
            val current = preferences[DOWNLOAD_COUNT_KEY] ?: 0
            updated = current + 1
            preferences[DOWNLOAD_COUNT_KEY] = updated
        }
        return updated
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