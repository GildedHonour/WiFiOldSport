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
                "CREATE TABLE " + WIFI_ACCESS_POINTS_TABLE_NAME  +
                        "(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "essid TEXT, " +
                        "bssid TEXT, " +
                        "ssid TEXT" +
                        ");"
    }


    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("not implemented")
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(ACCESS_POINTS_TABLE_CREATE)
        populateDb(db)
        db.close()
    }

    private fun populateDb(db: SQLiteDatabase) {
        TODO()
    }

    //todo: bssid? ssid?
    fun isBssIdAllowed(ssid: String): Boolean {
        val db = writableDatabase
        val cursor = db.query("todo_table_name???", arrayOf("id"), "ssid =?", arrayOf(ssid), null, null, null, null)
        val res = cursor.count > 0
        cursor.close()
        db.close()
        return res
    }
}


/*
SQLiteDatabase database = helper.getWritableDatabase();
database.insert(…);
database.close();
*/