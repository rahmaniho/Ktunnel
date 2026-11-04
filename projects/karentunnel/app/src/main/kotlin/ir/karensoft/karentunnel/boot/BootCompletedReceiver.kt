package ir.karensoft.karentunnel.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Restores the tunnel after reboot **only** if the user asked for it.
 *
 * Compliance notes:
 *  * We do not start anything on boot by default. Auto-starting a VPN that the
 *    user did not ask for is a privacy violation, and on Android 12+ background
 *    FGS starts are restricted anyway.
 *  * If always-on VPN is configured in system settings, the *system* starts our
 *    service; that path is handled in the service itself, not here.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val restore = prefs.getBoolean(KEY_RESTORE_ON_BOOT, false)
        val hadServer = prefs.getString(KEY_LAST_SERVER, null)
        if (!restore || hadServer.isNullOrBlank()) {
            Log.i(TAG, "boot: restore disabled or no server; staying disconnected")
            return
        }

        // Delegated to the app process so the FGS + tunnel start in the right
        // order (see docs/TECHNICAL-FEASIBILITY.md F2). If the platform blocks
        // the background start, we surface it in the UI instead of retrying.
        Log.i(TAG, "boot: restore requested by user preference")
        RestoreRequest.pending = true
    }

    companion object {
        private const val TAG = "BootReceiver"
        const val PREFS = "karentunnel_boot"
        const val KEY_RESTORE_ON_BOOT = "restore_on_boot"
        const val KEY_LAST_SERVER = "last_server_id"
    }
}

/** In-process handoff; deliberately not persisted (a failed start must be visible). */
object RestoreRequest {
    @Volatile
    var pending: Boolean = false
}
