package alexmaslakov.me.wifi_old_sport

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(ctx: Context): SQLiteOpenHelper(ctx, DB_NAME, null, DB_VERSION) {
    companion object {
        private val DB_NAME = "wifi_old_sport.db"
        private val TAG = "DbHelper"
        private val DB_VERSION = 2
        private val WIFI_ACCESS_POINTS_TABLE_NAME = "wifi_access_points"
        private val ACCESS_POINTS_TABLE_CREATE =
                "CREATE TABLE " + WIFI_ACCESS_POINTS_TABLE_NAME  +
                        " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "essid TEXT" +
                        "bssid TEXT" +
                        "ssid TEXT" +
                        ");"
    }


    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("not implemented")
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(ACCESS_POINTS_TABLE_CREATE)
    }

    public fun getAll(): List<String> {
        TODO()
    }
}