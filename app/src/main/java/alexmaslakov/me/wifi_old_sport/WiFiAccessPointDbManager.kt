package alexmaslakov.me.wifi_old_sport

import alexmaslakov.me.wifi_old_sport.WiFiNetworksDbManager.Companion.DATABASE_NAME
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class WiFiAccessPointDbManager(ctx: Context) : SQLiteOpenHelper(ctx, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DICTIONARY_TABLE_CREATE)
    }

    companion object {
        private val DATABASE_VERSION = 1
        private val DICTIONARY_TABLE_NAME = "wifi_old_sport"
        private val DICTIONARY_TABLE_CREATE = "CREATE TABLE " + DICTIONARY_TABLE_NAME + " (" +
                KEY_WORD + " TEXT, " +
                KEY_DEFINITION + " TEXT);"
    }
}