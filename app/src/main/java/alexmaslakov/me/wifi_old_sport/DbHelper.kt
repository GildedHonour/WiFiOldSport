package alexmaslakov.me.wifi_old_sport

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(ctx: Context): SQLiteOpenHelper(ctx, DB_NAME, null, DB_VERSION) {
    companion object {
        private val DB_NAME = "data.sqlite"
        private val TAG = "DbHelper"
        private val DB_VERSION = 1
        private val WIFI_ACCESS_POINTS_TABLE_NAME = "wifi_access_points"
        private val ACCESS_POINTS_TABLE_CREATE =
                "CREATE TABLE " + WIFI_ACCESS_POINTS_TABLE_NAME  + "(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "ssid TEXT" +
                        "bssid TEXT" +
                        ");"


        private val WIFI_ACCESS_POINT_ITEMS_TABLE_NAME = "wifi_access_point_items"
        private val ACCESS_POINT_ITEMS_TABLE_CREATE =
                "CREATE TABLE " + WIFI_ACCESS_POINT_ITEMS_TABLE_NAME  + "(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "ssid_id INTEGER references " + WIFI_ACCESS_POINTS_TABLE_NAME + " (id)" +
                        "bssid TEXT" +
                        "trust_level_id INTEGER" +
                        ");"
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("not implemented")
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(ACCESS_POINTS_TABLE_CREATE)
        db.execSQL(ACCESS_POINT_ITEMS_TABLE_CREATE)
        populateDb(db)
//        db.close()
    }

    private fun populateDb(db: SQLiteDatabase) {
        TODO()
    }

    //todo: bssid? ssid?
    fun isBssIdEnabled(ssid: String, bssid: String): Boolean {
        val db = writableDatabase
        val cursor = db.query("todo_table_name???", arrayOf("id"), "ssid =?", arrayOf(ssid), null, null, null, null)
        val res = cursor.count > 0
        cursor.close()
        db.close()
        return res
    }

    fun getSingleBssId(ssid: String, )
}

enum class WifiAccessPointTrustLevel(val x: Int) {
    UNKNOWN(0),
    ALWAYS(1),
    ONCE(2),
    UNTRUSTED(3),
}

/*
SQLiteDatabase database = helper.writableDatabase;
database.insert(…);
database.close();
*/

/*
ssid -- parent, has many bssid - name
bssid -- child, belongs to ssid - MAC address


todo - create 2 tables:
    wifi_access_points(id, ssid)
    wifi_access_point_items(id, wifi_access_point_ssid_id, bssid, status_id)
 */