package alexmaslakov.me.wifi_old_sport

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

class NotificationManagerEx(val ctx: Context?) {
    companion object {
        val PERMISSION_NOTIFICATION_ID = 0
        val LOCATION_NOTIFICATION_ID = 81
    }

    var notiffMgr = ctx!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun askWiFiAccessPointPermission(SSID: String, BSSID: String) {
        // Intent that will be used when the user allows the network
        val addIntent = Intent(ctx, WiFiAccessPointPermissionReceiver::class.java)
        addIntent.putExtra("SSID", SSID).putExtra("BSSID", BSSID).putExtra("enable", true)
        val addPendingIntent = PendingIntent.getBroadcast(ctx, 0, addIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        // Intent that will be used when the user blocks the network
        val disableIntent = Intent(ctx, WiFiAccessPointPermissionReceiver::class.java)
        disableIntent.putExtra("SSID", SSID).putExtra("BSSID", BSSID).putExtra("enable", false)
        val disablePendingIntent = PendingIntent.getBroadcast(ctx, 1, disableIntent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    fun disableWiFiNetworksNotifications() {
        notiffMgr.cancel(PERMISSION_NOTIFICATION_ID)
    }

    fun cancelPermissionRequest() {
        notiffMgr.cancel(PERMISSION_NOTIFICATION_ID)
    }

}