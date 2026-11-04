package ir.karensoft.karentunnel.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Everything the app remembers about the user's *preferences* (as opposed to
 * their tunnel configs, which live encrypted in [SecureConfigStore]).
 *
 * Rules that this type exists to enforce:
 *  * no secret ever reaches DataStore — it is plaintext XML/Preferences on disk.
 *    Only server *ids* and booleans live here; keys stay in the encrypted store.
 *  * every read has a defined default, so a fresh install and a corrupted file
 *    behave identically and never crash the dashboard.
 */
data class Settings(
    /** Server the dashboard shows. Null means "nothing selected yet". */
    val selectedServerId: String? = null,
    /** Reconnect automatically when the physical network changes. */
    val reconnectOnNetworkChange: Boolean = true,
    /** Start the tunnel after the device boots, only if it was up when it died. */
    val restoreOnBoot: Boolean = false,
    /** Ask the OS to keep the process alive for the tunnel (always-on hint). */
    val preferAlwaysOn: Boolean = false,
    /** Confirm before removing a server, because the key is unrecoverable. */
    val confirmDestructiveActions: Boolean = true,
)

interface SettingsRepository {
    val settings: Flow<Settings>
    suspend fun setSelectedServer(id: String?)
    suspend fun setReconnectOnNetworkChange(value: Boolean)
    suspend fun setRestoreOnBoot(value: Boolean)
    suspend fun setPreferAlwaysOn(value: Boolean)
    suspend fun setConfirmDestructiveActions(value: Boolean)
}

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "karentunnel.settings",
)

/**
 * DataStore-backed implementation.
 *
 * `updateData` is used rather than a read-modify-write pair so two rapid toggles
 * cannot lose one another.
 */
class DataStoreSettingsRepository(
    private val context: Context,
) : SettingsRepository {

    private object Keys {
        val SELECTED_SERVER = stringPreferencesKey("selected_server_id")
        val RECONNECT_ON_NETWORK_CHANGE = booleanPreferencesKey("reconnect_on_network_change")
        val RESTORE_ON_BOOT = booleanPreferencesKey("restore_on_boot")
        val PREFER_ALWAYS_ON = booleanPreferencesKey("prefer_always_on")
        val CONFIRM_DESTRUCTIVE = booleanPreferencesKey("confirm_destructive_actions")
    }

    override val settings: Flow<Settings> =
        context.settingsDataStore.data.map { prefs ->
            val defaults = Settings()
            Settings(
                selectedServerId = prefs[Keys.SELECTED_SERVER],
                reconnectOnNetworkChange = prefs[Keys.RECONNECT_ON_NETWORK_CHANGE]
                    ?: defaults.reconnectOnNetworkChange,
                restoreOnBoot = prefs[Keys.RESTORE_ON_BOOT] ?: defaults.restoreOnBoot,
                preferAlwaysOn = prefs[Keys.PREFER_ALWAYS_ON] ?: defaults.preferAlwaysOn,
                confirmDestructiveActions = prefs[Keys.CONFIRM_DESTRUCTIVE]
                    ?: defaults.confirmDestructiveActions,
            )
        }

    override suspend fun setSelectedServer(id: String?) {
        context.settingsDataStore.edit { prefs ->
            if (id == null) prefs.remove(Keys.SELECTED_SERVER) else prefs[Keys.SELECTED_SERVER] = id
        }
    }

    override suspend fun setReconnectOnNetworkChange(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.RECONNECT_ON_NETWORK_CHANGE] = value }
    }

    override suspend fun setRestoreOnBoot(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.RESTORE_ON_BOOT] = value }
    }

    override suspend fun setPreferAlwaysOn(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.PREFER_ALWAYS_ON] = value }
    }

    override suspend fun setConfirmDestructiveActions(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.CONFIRM_DESTRUCTIVE] = value }
    }
}
