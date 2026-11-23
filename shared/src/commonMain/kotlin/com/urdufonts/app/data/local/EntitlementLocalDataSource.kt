package com.urdufonts.app.data.local

import com.urdufonts.app.platform.currentTimeMillis

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.urdufonts.app.domain.models.SubscriptionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map


class EntitlementLocalDataSource(private val store: DataStore<Preferences>) {

    companion object {
        private val KEY_EXTERNAL_USER_ID = stringPreferencesKey("entitlement_external_user_id")
        private val KEY_ENTITLEMENTS_JSON = stringPreferencesKey("entitlements_json")
        private val KEY_IS_PRO_UNLOCKED = booleanPreferencesKey("is_pro_unlocked")
        private val KEY_PURCHASED_PLAN_TYPE = stringPreferencesKey("purchased_plan_type")
        private val KEY_LAST_REFRESH_TS = longPreferencesKey("entitlement_last_refresh_ts")
    }

    val entitlementsJsonFlow: Flow<String?> = store.data
        .map { preferences -> preferences[KEY_ENTITLEMENTS_JSON] }

    val isProUnlockedFlow: Flow<Boolean> = store.data
        .map { preferences -> preferences[KEY_IS_PRO_UNLOCKED] == true }

    val externalUserIdFlow: Flow<String?> = store.data
        .map { preferences -> preferences[KEY_EXTERNAL_USER_ID] }

    val lastRefreshTsFlow: Flow<Long> = store.data
        .map { preferences -> preferences[KEY_LAST_REFRESH_TS] ?: 0L }

    val purchasedPlanTypeFlow: Flow<SubscriptionType?> = store.data
        .map { preferences ->
            preferences[KEY_PURCHASED_PLAN_TYPE]?.let { name -> SubscriptionType.entries.firstOrNull { it.name == name } }
        }

    suspend fun saveExternalUserId(userId: String) {
        store.edit { preferences ->
            preferences[KEY_EXTERNAL_USER_ID] = userId
        }
    }

    suspend fun getExternalUserId(): String? {
        return externalUserIdFlow.firstOrNull()
    }

    suspend fun saveEntitlements(json: String, unlocked: Boolean) {
        store.edit { preferences ->
            preferences[KEY_ENTITLEMENTS_JSON] = json
            preferences[KEY_IS_PRO_UNLOCKED] = unlocked
            preferences[KEY_LAST_REFRESH_TS] = currentTimeMillis()
        }
    }

    suspend fun savePlanType(type: SubscriptionType?) {
        store.edit { preferences ->
            if (type != null) {
                preferences[KEY_PURCHASED_PLAN_TYPE] = type.name
            } else {
                preferences.remove(KEY_PURCHASED_PLAN_TYPE)
            }
        }
    }

    suspend fun clearEntitlements() {
        store.edit { preferences ->
            preferences.remove(KEY_EXTERNAL_USER_ID)
            preferences.remove(KEY_ENTITLEMENTS_JSON)
            preferences[KEY_IS_PRO_UNLOCKED] = false
            preferences.remove(KEY_PURCHASED_PLAN_TYPE)
            preferences.remove(KEY_LAST_REFRESH_TS)
        }
    }
}
