package alexmaslakov.me.wifi_old_sport

import android.app.NotificationManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.R.string.cancel

class NotificationManagerEx(val ctx: Context?) {
    var notiffMgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun askWiFiNetworkPermission(SSID: String, BSSID: String) {
        // Intent that will be used when the user allows the network
        val addIntent = Intent(ctx, WiFiNetworkPermissionManager::class.java)
        addIntent.putExtra("SSID", SSID).putExtra("BSSID", BSSID).putExtra("enable", true)
        val addPendingIntent = PendingIntent.getBroadcast(ctx, 0, addIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        // Intent that will be used when the user blocks the network
        val disableIntent = Intent(ctx, WiFiNetworkPermissionManager::class.java)
        disableIntent.putExtra("SSID", SSID).putExtra("BSSID", BSSID).putExtra("enable", false)
        val disablePendingIntent = PendingIntent.getBroadcast(ctx, 1, disableIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        // Build the notification dynamically, based on the network name
        val res = ctx!!.resources
        val headerString = String.format(res.getString(R.string.permission_header), SSID)
        val permissionString = String.format(res.getString(R.string.ask_permission), SSID)
        val yes = res.getString(R.string.yes)
        val no = res.getString(R.string.no)

        // NotificationCompat makes sure that the notification will also work on Android <4.0
        val notificationBuilder = NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(Notification.PRIORITY_MAX) // To force it to be first in list (and thus, expand)
                .setContentTitle(headerString)
                .setContentText(permissionString)
                .setStyle(NotificationCompat.BigTextStyle().bigText(permissionString))
                .setContentIntent(activityPendingIntent)
                .addAction(android.R.drawable.ic_delete, no, disablePendingIntent)
                .addAction(android.R.drawable.ic_input_add, yes, addPendingIntent)
        notiffMgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notiffMgr.notify(PERMISSION_NOTIFICATION_ID, notificationBuilder.build())
    }

    fun disableWiFiNetworksNotifications() {
        notiffMgr.cancel(PERMISSION_NOTIFICATION_ID)
    }

    fun cancelPermissionRequest() {
        notiffMgr.cancel(PERMISSION_NOTIFICATION_ID)
    }

    companion object {
        val PERMISSION_NOTIFICATION_ID = 0
        val LOCATION_NOTIFICATION_ID = 81
    }
}