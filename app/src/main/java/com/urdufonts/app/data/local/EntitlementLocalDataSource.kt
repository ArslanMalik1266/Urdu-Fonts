package com.urdufonts.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import com.urdufonts.app.domain.models.SubscriptionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.entitlementDataStore: DataStore<Preferences> by preferencesDataStore(name = "entitlement_preferences")

class EntitlementLocalDataSource(private val context: Context) {

    companion object {
        private val KEY_EXTERNAL_USER_ID = stringPreferencesKey("entitlement_external_user_id")
        private val KEY_ENTITLEMENTS_JSON = stringPreferencesKey("entitlements_json")
        private val KEY_IS_PRO_UNLOCKED = booleanPreferencesKey("is_pro_unlocked")
        private val KEY_PURCHASED_PLAN_TYPE = stringPreferencesKey("purchased_plan_type")
        private val KEY_LAST_REFRESH_TS = longPreferencesKey("entitlement_last_refresh_ts")
    }

    val entitlementsJsonFlow: Flow<String?> = context.entitlementDataStore.data
        .map { preferences -> preferences[KEY_ENTITLEMENTS_JSON] }

    val isProUnlockedFlow: Flow<Boolean> = context.entitlementDataStore.data
        .map { preferences -> preferences[KEY_IS_PRO_UNLOCKED] == true }

    val externalUserIdFlow: Flow<String?> = context.entitlementDataStore.data
        .map { preferences -> preferences[KEY_EXTERNAL_USER_ID] }

    val lastRefreshTsFlow: Flow<Long> = context.entitlementDataStore.data
        .map { preferences -> preferences[KEY_LAST_REFRESH_TS] ?: 0L }

    val purchasedPlanTypeFlow: Flow<SubscriptionType?> = context.entitlementDataStore.data
        .map { preferences ->
            when (preferences[KEY_PURCHASED_PLAN_TYPE]) {
                "MONTHLY" -> SubscriptionType.MONTHLY
                "SIX_MONTHS" -> SubscriptionType.SIX_MONTHS
                "LIFETIME" -> SubscriptionType.LIFETIME
                else -> null
            }
        }

    suspend fun saveExternalUserId(userId: String) {
        context.entitlementDataStore.edit { preferences ->
            preferences[KEY_EXTERNAL_USER_ID] = userId
        }
    }

    suspend fun getExternalUserId(): String? {
        return externalUserIdFlow.firstOrNull()
    }

    suspend fun saveEntitlements(json: String, unlocked: Boolean) {
        context.entitlementDataStore.edit { preferences ->
            preferences[KEY_ENTITLEMENTS_JSON] = json
            preferences[KEY_IS_PRO_UNLOCKED] = unlocked
            preferences[KEY_LAST_REFRESH_TS] = System.currentTimeMillis()
        }
    }

    suspend fun savePlanType(type: SubscriptionType?) {
        context.entitlementDataStore.edit { preferences ->
            if (type != null) {
                preferences[KEY_PURCHASED_PLAN_TYPE] = type.name
            } else {
                preferences.remove(KEY_PURCHASED_PLAN_TYPE)
            }
        }
    }

    suspend fun clearEntitlements() {
        context.entitlementDataStore.edit { preferences ->
            preferences.remove(KEY_EXTERNAL_USER_ID)
            preferences.remove(KEY_ENTITLEMENTS_JSON)
            preferences[KEY_IS_PRO_UNLOCKED] = false
            preferences.remove(KEY_PURCHASED_PLAN_TYPE)
            preferences.remove(KEY_LAST_REFRESH_TS)
        }
    }
}
