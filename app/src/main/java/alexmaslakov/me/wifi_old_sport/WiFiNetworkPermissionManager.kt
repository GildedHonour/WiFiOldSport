package alexmaslakov.me.wifi_old_sport

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import android.content.Intent
import android.util.Log

class WiFiNetworkPermissionManager: BroadcastReceiver() {
    private var ctx: Context? = null
    private var prefs: PreferencesStorage? = null

    override fun onReceive(context: Context, intent: Intent) {
        ctx = context
        prefs = PreferencesStorage(ctx)

        // Remove the notification that was used to make the decision
        removeNotification()

        val enable = intent.getBooleanExtra("enable", true)
        val SSID = intent.getStringExtra("SSID")
        val BSSID = intent.getStringExtra("BSSID")

        if (SSID == null || BSSID == null) {
            Log.e("PrivacyPolice", "Could not set permission because SSID or BSSID was null!")
            return
        }

        if (enable) {
            prefs!!.addAllowedBSSIDsForLocation(SSID)
            // initiate rescan, to make sure our algorithm enables the network, and to make sure
            // that Android connects to it
            val wifiManager = ctx!!.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiManager.startScan()
        } else {
            prefs!!.addBlockedBSSID(SSID, BSSID)
        }
    }

    private fun removeNotification() {
        val notificationHandler = NotificationManagerEx(ctx)
        notificationHandler.cancelPermissionRequest()
    }
}