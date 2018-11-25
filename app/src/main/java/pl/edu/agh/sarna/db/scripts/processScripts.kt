package pl.edu.agh.sarna.db.scripts

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.provider.BaseColumns
import android.util.Log
import pl.edu.agh.sarna.db.DbHelper
import pl.edu.agh.sarna.db.model.Processes
import pl.edu.agh.sarna.db.model.smsToken.TokenSmsDetails
import pl.edu.agh.sarna.model.SubtaskStatus
import pl.edu.agh.sarna.smsToken.model.Mode
import pl.edu.agh.sarna.utils.kotlin.toBoolean
import java.util.*

fun launchDatabaseConnection(context: Context, educationalMode: Boolean,
                             reportMode: Boolean, serverMode: Boolean, rootAllowed: Boolean): Long? {
    val dbHelper = DbHelper.getInstance(context)
    val db = dbHelper!!.writableDatabase

    val values = ContentValues().apply {
        put(Processes.ProcessEntry.COLUMN_NAME_START_TIME, Calendar.getInstance().timeInMillis.toString())
        put(Processes.ProcessEntry.COLUMN_NAME_SYSTEM_VERSION, Build.VERSION.SDK_INT)
        put(Processes.ProcessEntry.COLUMN_NAME_EDUCATIONAL, if (educationalMode) 1 else 0)
        put(Processes.ProcessEntry.COLUMN_NAME_REPORT, if (reportMode) 1 else 0)
        put(Processes.ProcessEntry.COLUMN_NAME_EXTERNAL_SERVER, if (serverMode) 1 else 0)
        put(Processes.ProcessEntry.COLUMN_NAME_ROOT_ALLOWED, if (rootAllowed) 1 else 0)
    }

    val processID = db?.insert(Processes.ProcessEntry.TABLE_NAME, null, values)
    Log.i("ID", "New process ID = $processID")

    return processID
}

fun updateProcess(context: Context, processID: Long) {
    val db = DbHelper.getInstance(context)

    val cv = ContentValues()
    cv.put(Processes.ProcessEntry.COLUMN_NAME_END_TIME, Calendar.getInstance().timeInMillis.toString())
    db!!.writableDatabase.update(Processes.ProcessEntry.TABLE_NAME, cv, "_id = ?", arrayOf(processID.toString()));

}

fun singleMethodReport(db: SQLiteDatabase?, processID: Long, tableName : String, statusColumn: String, processColumn: String, title : String) : SubtaskStatus?{
    val cursor = db!!.query(
            tableName,
            arrayOf(BaseColumns._ID, statusColumn),
            "$processColumn=?",
            arrayOf(processID.toString()),
            null, null,
            "_id DESC" ,
            "1"
    )
    if (cursor.moveToFirst()) {
        return SubtaskStatus(
                title,
                cursor.getInt(cursor.getColumnIndex(statusColumn)).toBoolean(),
                cursor.getLong(cursor.getColumnIndex(BaseColumns._ID)))
    }
    cursor.close()
    return SubtaskStatus(title, false)
}
fun smsMethodReport(db: SQLiteDatabase?, processID: Long, mode: Mode, title: String) : SubtaskStatus?{
    Log.i("BAZA", "Dane: $processID, mode: $mode, tytul $title")
    var cursor = db!!.query(
            TokenSmsDetails.TokenSmsDetailsEntry.TABLE_NAME,
            arrayOf(BaseColumns._ID, TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_STATUS),
            "${TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_PROCESS_ID}=? and ${TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_MODE}=?",
            arrayOf(processID.toString(), mode.ordinal.toString()),
            null, null,
            "_id DESC" ,
            "1"
    )
    if (cursor.moveToFirst()) {
        Log.i("BAZA", "Przeszło z id = ${cursor.getLong(cursor.getColumnIndex(BaseColumns._ID))} ")
        return SubtaskStatus(
                title,
                cursor.getInt(cursor.getColumnIndex( TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_STATUS)).toBoolean(),
                cursor.getLong(cursor.getColumnIndex(BaseColumns._ID)))
    }
    else{
        val testMode = if(mode == Mode.NOT_SAFE) Mode.TEST else Mode.TEST_DUMMY
        cursor = db.query(
                TokenSmsDetails.TokenSmsDetailsEntry.TABLE_NAME,
                arrayOf(BaseColumns._ID),
                "${TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_PROCESS_ID}=? and ${TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_MODE}=?",
                arrayOf(processID.toString(), testMode.ordinal.toString()),
                null, null,
                "_id DESC" ,
                "1"
        )
        if (cursor.moveToFirst()) {
            Log.i("BAZA", "Przeszło z id = ${cursor.getLong(cursor.getColumnIndex(BaseColumns._ID))} ")
            return SubtaskStatus(
                    title,
                    false,
                    cursor.getLong(cursor.getColumnIndex(BaseColumns._ID)))
        }
    }
    cursor.close()

    return SubtaskStatus(title, false)
}

fun getLastProcess(context: Context?) : Array<Any> {
    val dbHelper = DbHelper.getInstance(context!!)
    val db = dbHelper!!.writableDatabase
    val cursor = db!!.query(
            Processes.ProcessEntry.TABLE_NAME,
            arrayOf(BaseColumns._ID, Processes.ProcessEntry.COLUMN_NAME_ROOT_ALLOWED, Processes.ProcessEntry.COLUMN_NAME_EXTERNAL_SERVER, Processes.ProcessEntry.COLUMN_NAME_REPORT),
            null,
            null,
            null, null,
            "_id DESC" ,
            "1"
    )
    if (cursor.moveToFirst()) {
        return arrayOf(
                cursor.getLong(cursor.getColumnIndex(BaseColumns._ID)),
                cursor.getInt(cursor.getColumnIndex(Processes.ProcessEntry.COLUMN_NAME_ROOT_ALLOWED)).toBoolean(),
                cursor.getInt(cursor.getColumnIndex(Processes.ProcessEntry.COLUMN_NAME_EXTERNAL_SERVER)).toBoolean(),
                cursor.getInt(cursor.getColumnIndex(Processes.ProcessEntry.COLUMN_NAME_REPORT)).toBoolean())
    }
    return arrayOf()
}
