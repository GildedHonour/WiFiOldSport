package alexmaslakov.me.wifi_old_sport

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Handler
import android.util.Log

class WiFiAccessPointScanReceiver: BroadcastReceiver() {
    //todo
    companion object {
        private var lastCheck: Long = 0
        private var wifiManager: WifiManager? = null
        private var connectivityManager: ConnectivityManager? = null
        private var context: Context? = null
        val FREQUENCY = 500
    }

    enum class AccessPointSafety {
        TRUSTED_ALWAYS, TRUSTED_THIS_TIME_ONLY, UNTRUSTED, UNKNOWN
    }

    var notif: NotificationManagerEx? = null
    var db: DbManager? = null

    override fun onReceive(ctx: Context, intent: Intent) {
        if (wifiManager == null) {
            wifiManager = ctx.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            connectivityManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            notif = NotificationManagerEx(ctx)
            context = ctx
            db = DbManager(ctx)
        }
        // Make sure the wakelockHandler keeps running (to prevent Android 6.0 and up from completely suspending our operations)
//        WakelockHandler.getInstance(ctx).ensureAwake()

        // WiFi scan performed
        // Older devices might try to scan constantly. Allow them some rest by checking max. once every 0.5 seconds
        if (System.currentTimeMillis() - lastCheck < FREQUENCY) {
            return
        }

        lastCheck = System.currentTimeMillis()

        // Check our location permission, and request if needed
//            LocationAccess.checkAccessDisplayNotification(context)

        try {
            checkResults(wifiManager!!.scanResults)
        } catch (e: NullPointerException) {
            Log.e("PrivacyPolice", "Null pointer exception when handling networks. Wi-Fi was probably suddenly disabled after a scan", e)
        }
    }

    private fun checkResults(scanResults: List<ScanResult>) {
        // Keep whether the getNetworkSafety function asked the user for input (to know whether we
        // have to disable any notifications afterwards, and to keep the UX as smooth as possible).
        // Alternatively, we would disable previous notifications here, but that would lead to the
        // notification being jittery (disappearing and re-appearing instantly, instead of just
        // updating).
        var notificationShown = false

        // Collect number of found networks, if allowed by user
        /*Analytics analytics = new Analytics(ctx);
    analytics.scanCompleted(scanResults.size());*/

        val networkList = wifiManager!!.configuredNetworks
        if (networkList == null) {
            Log.i("PrivacyPolice", "WifiManager did not return any configured networks. This is " +
                    "most likely caused by background location services being allowed to scan for " +
                    "Wi-Fi networks, while Wi-Fi is disabled. Keep all networks as before.")
            return
        }
        // Check for every network in our network list whether it should be enabled
        for (network in networkList!!) {
            val networkSafety = getNetworkSafety(network, scanResults)
            if (networkSafety == AccessPointSafety.TRUSTED_THIS_TIME_ONLY) {
                connectTo(network.networkId)
            } else if (networkSafety == AccessPointSafety.UNTRUSTED) {
                // Make sure all other networks are disabled, by disabling them separately
                // (See comment in connectTo() method to see why we don't disable all of them at the
                // same time)
                wifiManager!!.disableNetwork(network.networkId)
            } else if (networkSafety == AccessPointSafety.UNKNOWN) {
                wifiManager!!.disableNetwork(network.networkId)
                notificationShown = true
            }
        }

        if (!notificationShown) {
            notif!!.disableWiFiNetworksNotifications()
        }
    }

    private fun connectTo(networkId: Int) {
        // Do not disable other networks, as multiple networks may be available
        wifiManager!!.enableNetwork(networkId, false)
        // If we aren't already connected to a network, make sure that Android connects.
        // This is required for devices running Android Lollipop (5.0) and up, because
        // they would otherwise never connect.
        wifiManager!!.reconnect()
        // In some instances (since wpa_supplicant 2.3), even the previous is not sufficient
        // Check if we are in a CONNECTING state, or reassociate to force connection
        val handler = Handler()
        // Wait for 1 second before checking
        handler.postDelayed({
            val wifiState = connectivityManager!!.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            if (!wifiState.isConnectedOrConnecting()) {
                Log.i("PrivacyPolice", "Reassociating, because WifiManager doesn't seem to be eager to reconnect.")
                wifiManager!!.reassociate()
            }
        }, 1000)
    }

    fun getNetworkSafety(network: WifiConfiguration, scanResults: List<ScanResult>): AccessPointSafety {
        // If all settings are disabled by the user, then allow every network
        // This effectively disables all of the app's functionalities

        //todo replace with sqlite
//        if (!(prefs!!.getEnableOnlyAvailableNetworks() || prefs!!.getOnlyConnectToKnownAccessPoints())) {
//            return AccessPointSafety.TRUSTED_THIS_TIME_ONLY // Allow every network
//        }




        // If location access is disabled by the user (or if it is not granted to our app), allow
        // every network (as otherwise PrivacyPolice would block normal operation of the phone).
        // Rationale: a huge warning is displayed both as a notification, and in the main activity
        // when the user does not enable location access. It is unfortunately the only way for us
        // to view scan results
        // Some devices still allow scan results to be passed on even if the location is disabled.
        // In this case, we operate as normally by checking if any network is in range
//            if (!LocationAccess.isNetworkLocationEnabled(context) && scanResults.size == 0) {
//                return AccessPointSafety.TRUSTED // Allow every network
//            }

        // Always enable hidden networks, since they *need* to use directed probe requests
        // in order to be discovered. Note that using hidden SSID's does not add any
        // real security , so it's best to avoid them whenever possible.
        if (network.hiddenSSID) {
            return AccessPointSafety.TRUSTED_THIS_TIME_ONLY
        }

        val plainSSID = network.SSID.replace("\"", "")
        scanResults
                .filter { it.SSID == plainSSID }
                .forEach {
                    // Check whether the user wants to filter by MAC address
                    //todo replace with sqlite
                    if (!prefs!!.getOnlyConnectToKnownAccessPoints()) { // Any MAC address is fair game
                        // Enabling now makes sure that we only want to connect when it is in range
                        return AccessPointSafety.TRUSTED_THIS_TIME_ONLY
                    } else { // Check access point's MAC address
                        // Check if the MAC address is in the list of allowed MAC's for this SSID
                        //todo replace with sqlite
                        val allowedBSSIDs = prefs!!.getAllowedBSSIDs(it.SSID)
                        return if (allowedBSSIDs.contains(it.BSSID)) {
                            AccessPointSafety.TRUSTED_THIS_TIME_ONLY
                        } else {
                            // Not an allowed BSSID
                            //todo replace with sqlite
                            if (prefs!!.getBlockedBSSIDs(it.SSID).contains(it.BSSID)) {
                                // This SSID was explicitly blocked by the user!
                                Log.w("PrivacyPolice", "Spoofed network for " + it.SSID + " detected! (BSSID is " + it.BSSID + ")")
                                AccessPointSafety.UNTRUSTED
                            } else {
                                // We don't know yet whether the user wants to allow this network
                                // Ask the user what needs to be done
                                notif!!.askWiFiAccessPointPermission(it.SSID, it.BSSID)
                                AccessPointSafety.UNKNOWN
                            }
                        }
                    }
                }

        return AccessPointSafety.UNTRUSTED // Network not in range
    }
}
