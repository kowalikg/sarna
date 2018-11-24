package pl.edu.agh.sarna.db.scripts

import android.content.ContentValues
import android.content.Context
import android.database.DatabaseUtils
import android.provider.BaseColumns
import android.util.Log
import pl.edu.agh.sarna.db.DbHelper
import pl.edu.agh.sarna.db.model.cloak.CloakInfo
import pl.edu.agh.sarna.db.model.cloak.CloakText
import pl.edu.agh.sarna.db.model.wifi.WifiPasswords
import pl.edu.agh.sarna.model.SubtaskStatus
import pl.edu.agh.sarna.utils.kotlin.toInt
import java.util.*
import kotlin.collections.ArrayList

fun insertCloakQuery(context: Context, processID: Long) : Long? {
    val dbHelper = DbHelper.getInstance(context)
    val db = dbHelper!!.writableDatabase

    val values = ContentValues().apply {
        put(CloakInfo.CloakInfoEntry.COLUMN_NAME_PROCESS_ID, processID)
        put(CloakInfo.CloakInfoEntry.COLUMN_NAME_START_TIME, Calendar.getInstance().timeInMillis.toString())
    }

    val runID = db?.insert(CloakInfo.CloakInfoEntry.TABLE_NAME, null, values)
    Log.i("ID", "New run ID = $runID")

    return runID
}

fun insertCloakText(context: Context, runID: Long, text: String, packageName: String) {
    val dbHelper = DbHelper.getInstance(context)
    val db = dbHelper!!.writableDatabase
    val values = ContentValues().apply {
        put(CloakText.CloakTextEntry.COLUMN_NAME_RUN_ID, runID)
        put(CloakText.CloakTextEntry.COLUMN_NAME_PACKAGE, packageName)
        put(CloakText.CloakTextEntry.COLUMN_NAME_TEXT, text)
    }

    val tryID = db?.insert(CloakText.CloakTextEntry.TABLE_NAME, null, values)
    Log.i("ID", "New runutils ID = $tryID")
}
fun updateCloakMethod(context: Context, runID: Long, status: Boolean) {
    val db = DbHelper.getInstance(context)!!.writableDatabase
    val cv = ContentValues()
    cv.put(CloakInfo.CloakInfoEntry.COLUMN_NAME_END_TIME, Calendar.getInstance().timeInMillis.toString())
    cv.put(CloakInfo.CloakInfoEntry.COLUMN_NAME_STATUS, status.toInt())

    db.update(CloakInfo.CloakInfoEntry.TABLE_NAME, cv, "${BaseColumns._ID} = ?", arrayOf(runID.toString()));
}
fun getLastCloakRunID(context: Context?) : Long {
    val dbHelper = DbHelper.getInstance(context!!)
    val db = dbHelper!!.writableDatabase
    val cursor = db!!.query(
            CloakInfo.CloakInfoEntry.TABLE_NAME,
            arrayOf(BaseColumns._ID),
            null,
            null,
            null, null,
            "_id DESC" ,
            "1"
    )
    if (cursor.moveToFirst()) {
        return cursor.getLong(cursor.getColumnIndex(BaseColumns._ID))
    }
    return -1
}

fun getTextByRunID(context: Context?, runID: Long) : ArrayList<SubtaskStatus> {
    val dbHelper = DbHelper.getInstance(context!!)
    val db = dbHelper!!.writableDatabase
    val cursor = db!!.query(
            CloakText.CloakTextEntry.TABLE_NAME,
            arrayOf(CloakText.CloakTextEntry.COLUMN_NAME_PACKAGE, CloakText.CloakTextEntry.COLUMN_NAME_TEXT),
            "${CloakText.CloakTextEntry.COLUMN_NAME_RUN_ID}=?",
            arrayOf(runID.toString()),
            null, null, null, null
    )
    val list = ArrayList<SubtaskStatus>()
    if (cursor.moveToFirst()) {
        while (cursor.moveToNext()){
            list.add(SubtaskStatus(cursor.getString(cursor.getColumnIndex(CloakText.CloakTextEntry.COLUMN_NAME_PACKAGE)),
                    cursor.getString(cursor.getColumnIndex(CloakText.CloakTextEntry.COLUMN_NAME_TEXT))))
        }
    }
    return list
}

fun textAmount(context: Context?, runID: Long): Long {
    val db = DbHelper.getInstance(context!!)!!.readableDatabase
    return DatabaseUtils.queryNumEntries(db, CloakText.CloakTextEntry.TABLE_NAME,
            "${CloakText.CloakTextEntry.COLUMN_NAME_RUN_ID}=?", arrayOf(runID.toString()))
}
