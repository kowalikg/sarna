package pl.edu.agh.sarna.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DbQueries.CREATE_PROCESS)
        db.execSQL(DbQueries.CREATE_WIFI_PASSWORDS)
        db.execSQL(DbQueries.CREATE_WIFI_UTILS)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(DbQueries.SQL_DELETE_ENTRIES)
        onCreate(db)
    }
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }
    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "SarnaDB.db"
        private var instance : DbHelper? = null

        @Synchronized
        fun getInstance(context: Context) : DbHelper? {
            if (instance == null){
                instance = DbHelper(context)
            }
            return instance
        }

    }
}