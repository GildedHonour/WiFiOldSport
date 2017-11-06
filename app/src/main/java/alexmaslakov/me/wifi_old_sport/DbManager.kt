package alexmaslakov.me.wifi_old_sport

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbManager(ctx: Context): SQLiteOpenHelper(ctx, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("not implemented")
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(ACCESS_POINTS_TABLE_CREATE)
    }

    companion object {
        private val DATABASE_VERSION = 1
        private val WIFI_ACCESS_POINTS_TABLE_NAME = "wifi_access_points"
        private val KEY_WORD = "id"
        private val KEY_DEFINITION = "todo"

        private val ACCESS_POINTS_TABLE_CREATE =
                "CREATE TABLE " + WIFI_ACCESS_POINTS_TABLE_NAME  +
                        " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "column2 TEXT" +
                        ");"

        private val DATABASE_NAME = "wifi_old_sport.db"
        private val TAG = "DbManager"
    }
}