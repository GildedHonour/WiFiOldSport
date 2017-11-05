package alexmaslakov.me.wifi_old_sport

import android.app.NotificationManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v7.app.NotificationCompat

class NotificationManagerEx(val ctx: Context) {
    val notifManag = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun askWiFiNetworkPermission(SSID: String, BSSID: String) {
        // Intent that will be used when the user allows the network
        val addIntent = Intent(ctx, PermissionChangeReceiver::class.java)
        addIntent.putExtra("SSID", SSID).putExtra("BSSID", BSSID).putExtra("enable", true)
        val addPendingIntent = PendingIntent.getBroadcast(context, 0, addIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        // Intent that will be used when the user blocks the network
        val disableIntent = Intent(ctx, PermissionChangeReceiver::class.java)
        disableIntent.putExtra("SSID", SSID).putExtra("BSSID", BSSID).putExtra("enable", false)
        val disablePendingIntent = PendingIntent.getBroadcast(context, 1, disableIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        // Intent that will be used when the user's OS does not support notification actions
        val activityIntent = Intent(ctx, AskPermissionActivity::class.java)
        activityIntent.putExtra("SSID", SSID).putExtra("BSSID", BSSID)
        val activityPendingIntent = PendingIntent.getActivity(context, 2, activityIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        // Build the notification dynamically, based on the network name
        val res = ctx.getResources()
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
        notifManag = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifManag.notify(PERMISSION_NOTIFICATION_ID, notificationBuilder.build())
    }
}