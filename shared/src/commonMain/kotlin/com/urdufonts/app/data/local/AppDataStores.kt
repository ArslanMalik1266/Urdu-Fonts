package com.urdufonts.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.urdufonts.app.platform.AppDirectories
import okio.Path.Companion.toPath

/**
 * The app's three preference stores. Each is created once (DataStore allows one instance per file).
 * The files live at `<filesDir>/datastore/<name>.preferences_pb`, exactly where Android's
 * `preferencesDataStore(name)` kept them, so updating the app keeps the user signed in.
 */
class AppDataStores(directories: AppDirectories) {
    val user: DataStore<Preferences> = create(directories, "user_preferences")
    val entitlement: DataStore<Preferences> = create(directories, "entitlement_preferences")
    val iap: DataStore<Preferences> = create(directories, "iap_preferences")

    private fun create(directories: AppDirectories, name: String): DataStore<Preferences> =
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { "${directories.filesDir}/datastore/$name.preferences_pb".toPath() }
        )
}
