package alexmaslakov.me.wifi_old_sport

import android.app.AlertDialog
import android.app.ListActivity
import android.content.Context
import android.content.DialogInterface
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*

abstract class WiFiAccessPointListBaseActivity : ListActivity() {
    protected var adapter: NetworkManagerAdapter? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    public override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.networkmanager, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            R.id.action_removeall -> {
                // Ask the user to confirm that he/she wants to remove all networks
                confirmClearAll()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Repopulate the list by getting the latest information on available networks, and
     * combining them with networks stored in the preferences.
     */
    fun refresh() {
        adapter!!.refresh()
    }

    /**
     * Ask the user for confirmation that he/she really wants to remove all trusted/untrusted
     * APs, and remove them if the user confirms.
     */
    abstract fun confirmClearAll()

    /**
     * Adapter that is responsible for populating the list of networks. In this case, the adapter
     * also contains all logic to sort the networks by availability, and for getting the list from
     * the preference storage.
     */
    protected abstract inner class NetworkManagerAdapter : BaseAdapter() {
        protected var prefs: PreferencesStorage? = null
        protected var wifiManager: WifiManager? = null
        private var layoutInflater: LayoutInflater? = null
        // Store the list of networks we know, together with their current availability
        protected var networkList: ArrayList<NetworkAvailability>? = null

        init {
            val context = this@WiFiAccessPointListBaseActivity.applicationContext
            prefs = PreferencesStorage(context)
            wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            // Creating the list for the first time
            refresh()
        }

        /**
         * Repopulate the list by getting the latest information on available networks, and
         * combining them with networks stored in the preferences.
         * Only displays networks that are stored in the preferences.
         */
        abstract fun refresh()

        override fun getCount(): Int {
            return networkList!!.size
        }

        override fun getItem(position: Int): Any {
            return networkList!![position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val layout: LinearLayout
            // Recycle a previous view, if available
            layout = if (convertView == null) {
                // Not available, create a new view
                layoutInflater!!.inflate(R.layout.item_networkmanager, null) as LinearLayout
            } else {
                convertView as LinearLayout?
            }

            // Fill in the text part of the layout with the NetworkAvailability
            val SSIDinfo = getItem(position) as NetworkAvailability
            val SSIDtext = layout.findViewById<View>(R.id.SSIDname) as TextView
            SSIDtext.text = SSIDinfo.name
            // Make the 'signal strength' icon visible if the network is available
            val signalStrengthImage = layout.findViewById<View>(R.id.signalStrength) as ImageView
            Log.v("PrivacyPolice", "Adding network " + SSIDinfo.name + " with signal strength " + SSIDinfo.signalStrength)
            // Color signal strength teal (if trusted) or pink (if blocked)
            var color = "teal"
            if (SSIDinfo.accessPointSafety === WiFiAccessPointScanReceiver.AccessPointSafety.UNTRUSTED)
                color = "pink"
            var resourceName = "ic_wifi_signal_" + SSIDinfo.signalStrength + "_" + color
            if (SSIDinfo.signalStrength == -1)
                resourceName = "ic_wifi_unavailable_" + color
            val resourceId = resources.getIdentifier(resourceName, "drawable", packageName)
            signalStrengthImage.setImageResource(resourceId)

            return layout
        }
    }

    protected inner class NetworkAvailability(name: String, rssi: Int, accessPointSafety: WiFiAccessPointScanReceiver.AccessPointSafety) {
        var name: String? = null

        var signalStrength: Int = 0
            set(rssi) = if (rssi < -999) {
                field = -1
            } else {
                field = WifiManager.calculateSignalLevel(rssi, 5)
            }

        var accessPointSafety: WiFiAccessPointScanReceiver.AccessPointSafety? = null

        val isAvailable: Boolean
            get() = this.signalStrength >= 0

        init {
            this.name = name
            this.signalStrength = rssi
            this.accessPointSafety = accessPointSafety
        }
    }
}


class WiFiAccessPointListActivity : WiFiAccessPointListBaseActivity() {
    var ssid: String? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ssid = getIntent().getStringExtra("SSID")
        setTitle(ssid)
        adapter = MACManagerAdapter()
        setListAdapter(adapter)
    }

    override fun onListItemClick(listView: ListView, view: View, position: Int, id: Long) {
        val listItem = listView.getItemAtPosition(position) as NetworkAvailability
        val mac = listItem.name
        Log.v("PrivacyPolice", "Asking for confirmation to remove mac $mac for network $ssid")
        // Ask for confirmation first
        val builder = AlertDialog.Builder(this)
        builder.setMessage(String.format(getResources().getString(R.string.dialog_removetrustedmac), mac))
        builder.setPositiveButton(R.string.dialog_remove, DialogInterface.OnClickListener { dialog, id ->
            // Actually remove the BSSID from the 'trusted' list
            val prefs = PreferencesStorage(this@MACManagerActivity)
            if (listItem.getAccessPointSafety() === ScanResultsChecker.AccessPointSafety.TRUSTED) {
                prefs.removeAllowedBSSID(ssid, mac)
            }             else {
                prefs.removeBlockedBSSID(ssid, mac)
            }

            this@MACManagerActivity.refresh()
        })
        builder.setNegativeButton(R.string.dialog_clearhotspots_no, DialogInterface.OnClickListener { dialog, id ->
            // User canceled
        })
        builder.show()
    }

    override fun confirmClearAll() {
        // Ask for confirmation first
        val builder = AlertDialog.Builder(this)
        builder.setMessage(String.format(getResources().getString(R.string.dialog_clearhotspotsformac), ssid))
        builder.setPositiveButton(R.string.dialog_clearhotspots_yes, DialogInterface.OnClickListener { dialog, id ->
            // Actually clear the list
            val prefs = PreferencesStorage(this@MACManagerActivity)
            prefs.clearBSSIDsForNetwork(this@MACManagerActivity.ssid)
            this@MACManagerActivity.refresh()
        })
        builder.setNegativeButton(R.string.dialog_clearhotspots_no, DialogInterface.OnClickListener { dialog, id ->
            // User canceled
        })
        builder.show()
    }

    inner class MACManagerAdapter : NetworkManagerAdapter() {
        override fun refresh() {
            Log.v("PrivacyPolice", "Refreshing the SSID list adapter")
            // Use an ArrayMap so we can put available access points at the top
            networkList = ArrayList()

            // Combine the access points that we know of with the access points that are available.
            val scanResults = wifiManager.getScanResults()

            val trustedMACs = prefs.getAllowedBSSIDs(ssid)
            // Add currently available access points that we trust to the list
            for (scanResult in scanResults) {
                if (trustedMACs.contains(scanResult.BSSID)) {
                    networkList.add(NetworkAvailability(scanResult.BSSID, scanResult.level, WiFiAccessPointScanReceiver.AccessPointSafety.TRUSTED))
                    trustedMACs.remove(scanResult.BSSID)
                }
            }
            val blockedMACs = prefs.getBlockedBSSIDs(ssid)
            // Add currently available access points that we block to the list
            for (scanResult in scanResults) {
                if (blockedMACs.contains(scanResult.BSSID)) {
                    networkList.add(NetworkAvailability(scanResult.BSSID, scanResult.level, WiFiAccessPointScanReceiver.AccessPointSafety.UNTRUSTED))
                    blockedMACs.remove(scanResult.BSSID)
                }
            }

            // Add all other (non-available) saved SSIDs to the list
            trustedMACs.forEach { x -> networkList.add(NetworkAvailability(x, -99999, WiFiAccessPointScanReceiver.AccessPointSafety.TRUSTED)) }
            blockedMACs.forEach { x -> networkList.add(NetworkAvailability(x, -99999, WiFiAccessPointScanReceiver.AccessPointSafety.UNTRUSTED)) }
            notifyDataSetChanged()
        }
    }
}