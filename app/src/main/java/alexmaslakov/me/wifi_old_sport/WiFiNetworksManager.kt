package alexmaslakov.me.wifi_old_sport

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class WiFiNetworksDbManager(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
//            db.execSQL(SQL_CREATE_ENTRIES)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
//            db.execSQL(SQL_DELETE_ENTRIES)
//            onCreate(db)
        }

        override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            onUpgrade(db, oldVersion, newVersion)
        }

        companion object {
            val DATABASE_VERSION = 1
            val DATABASE_NAME = "WiFiNetworks.db"
        }
}