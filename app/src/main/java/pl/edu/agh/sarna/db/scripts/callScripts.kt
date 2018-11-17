package pl.edu.agh.sarna.db.scripts

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.DatabaseUtils
import android.provider.BaseColumns
import android.util.Log
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.db.DbHelper
import pl.edu.agh.sarna.db.DbQueries
import pl.edu.agh.sarna.db.model.calls.CallsDetails
import pl.edu.agh.sarna.db.model.calls.CallsLogs
import pl.edu.agh.sarna.db.model.calls.CallsLogsInfo
import pl.edu.agh.sarna.db.model.contacts.Contacts
import pl.edu.agh.sarna.db.model.contacts.ContactsInfo
import pl.edu.agh.sarna.utils.kotlin.toInt
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

fun insertCallsQuery(context: Context?, processID: Long) : Long? {
    val dbHelper = DbHelper.getInstance(context!!)
    val db = dbHelper!!.writableDatabase

    val values = ContentValues().apply {
        put(CallsDetails.CallsDetailsEntry.COLUMN_NAME_PROCESS_ID, processID)
        put(CallsDetails.CallsDetailsEntry.COLUMN_NAME_START_TIME, Calendar.getInstance().timeInMillis.toString())
    }

    val runID = db?.insert(CallsDetails.CallsDetailsEntry.TABLE_NAME, null, values)
    Log.i("ID", "New run ID = $runID")

    return runID
}
fun insertContactsInfoQuery(context: Context?, runID: Long, contactsPermissionGranted: Boolean) : Long? {
    val dbHelper = DbHelper.getInstance(context!!)
    val db = dbHelper!!.writableDatabase

    val values = ContentValues().apply {
        put(ContactsInfo.ContactsInfoEntry.COLUMN_NAME_RUN_ID, runID)
        put(ContactsInfo.ContactsInfoEntry.COLUMN_NAME_CONTACTS_PERMISSION, contactsPermissionGranted)
    }

    val tryID = db?.insert(ContactsInfo.ContactsInfoEntry.TABLE_NAME, null, values)
    Log.i("ID", "New tryID = $tryID")

    return tryID
}

fun updateCallsMethod(context: Context?, runID: Long, status: Boolean) {
    val db = DbHelper.getInstance(context!!)!!.writableDatabase
    val cv = ContentValues()
    cv.put(CallsDetails.CallsDetailsEntry.COLUMN_NAME_END_TIME, Calendar.getInstance().timeInMillis.toString())
    cv.put(CallsDetails.CallsDetailsEntry.COLUMN_NAME_STATUS, status.toInt())

    db.update(CallsDetails.CallsDetailsEntry.TABLE_NAME, cv, "${BaseColumns._ID} = ?", arrayOf(runID.toString()));
}

fun insertCallsLogsInfoQuery(context: Context?, runID: Long, callLogsPermissionGranted: Boolean){
    val dbHelper = DbHelper.getInstance(context!!)
    val db = dbHelper!!.writableDatabase

    val values = ContentValues().apply {
        put(CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_RUN_ID, runID)
        put(CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_LOG_PERMISSION, callLogsPermissionGranted)
    }

    val tryID = db?.insert(CallsLogsInfo.CallsLogsInfoEntry.TABLE_NAME, null, values)
    Log.i("ID", "New run ID = $runID")

}

@SuppressLint("SimpleDateFormat")
fun insertCallsLogs(context: Context?, runID: Long, name: String?, number: String?, type: String?, date: String?, duration: String) : Long {
    val dbHelper = DbHelper.getInstance(context!!)
    val db = dbHelper!!.writableDatabase

    val values = ContentValues().apply {
        put(CallsLogs.CallsLogsEntry.COLUMN_NAME_RUN_ID, runID)
        put(CallsLogs.CallsLogsEntry.COLUMN_NAME_NAME, name)
        put(CallsLogs.CallsLogsEntry.COLUMN_NAME_NUMBER, number)
        put(CallsLogs.CallsLogsEntry.COLUMN_NAME_TYPE, type!!.toInt())
        put(CallsLogs.CallsLogsEntry.COLUMN_NAME_DATE, SimpleDateFormat("HH:mm:ss").format(Date(date!!.toLong()).time).toString())
        put(CallsLogs.CallsLogsEntry.COLUMN_NAME_DURATION, duration)
    }

    return db?.insert(CallsLogs.CallsLogsEntry.TABLE_NAME, null, values)!!
}

fun updateCallsLogsInfoQuery(context: Context?, runID: Long, status: Boolean) {
    val db = DbHelper.getInstance(context!!)!!.writableDatabase
    val cv = ContentValues()
    cv.put(CallsLogsInfo.CallsLogsInfoEntry.COLUMN_NAME_FOUND, status.toInt())
    db.update(CallsLogsInfo.CallsLogsInfoEntry.TABLE_NAME, cv, "${BaseColumns._ID} = ?", arrayOf(runID.toString()));
}

fun updateContactsInfoQuery(context: Context?, runID: Long, status: Boolean) {
    val db = DbHelper.getInstance(context!!)!!.writableDatabase
    val cv = ContentValues()
    cv.put(ContactsInfo.ContactsInfoEntry.COLUMN_NAME_FOUND, status.toInt())
    db.update(ContactsInfo.ContactsInfoEntry.TABLE_NAME, cv, "${BaseColumns._ID} = ?", arrayOf(runID.toString()));
}


fun callLogsAmount(context: Context?, runID: Long): Long {
    val db = DbHelper.getInstance(context!!)!!.readableDatabase
    return DatabaseUtils.queryNumEntries(db, CallsLogs.CallsLogsEntry.TABLE_NAME,
            "${CallsLogs.CallsLogsEntry.COLUMN_NAME_RUN_ID}=?", arrayOf(runID.toString()))
}

fun contactsAmount(context: Context?, runID: Long): Long {
    val db = DbHelper.getInstance(context!!)!!.readableDatabase
    return DatabaseUtils.queryNumEntries(db, Contacts.ContactsEntry.TABLE_NAME,
            "${Contacts.ContactsEntry.COLUMN_NAME_RUN_ID}=?", arrayOf(runID.toString()))
}

fun insertContacts(context: Context?, runID: Long, name: String?, number: String?) : Long {
    val dbHelper = DbHelper.getInstance(context!!)
    val db = dbHelper!!.writableDatabase

    val values = ContentValues().apply {
        put(Contacts.ContactsEntry.COLUMN_NAME_RUN_ID, runID)
        put(Contacts.ContactsEntry.COLUMN_NAME_NAME, name)
        put(Contacts.ContactsEntry.COLUMN_NAME_NUMBER, number)
    }

    return db?.insert(Contacts.ContactsEntry.TABLE_NAME, null, values)!!
}

fun mostFrequentContact(context: Context, runID: Long): String {
    val db = DbHelper.getInstance(context)!!.readableDatabase

    val cursor = db.rawQuery(DbQueries.MOST_FREQUENT_CONTACT, arrayOf(runID.toString()))

    if(cursor.moveToFirst()){
        return cursor.getString(1)?.let { cursor.getString(1) } ?: run {context.getString(R.string.not_in_contacts)}
    }
    return context.getString(R.string.not_found)
}

fun top5duration(context: Context, runID: Long) : Map<String, Float> {
    val map = HashMap<String, Float>()
    val db = DbHelper.getInstance(context)!!.readableDatabase
    val cursor = db.rawQuery(DbQueries.GET_DURATION, arrayOf(runID.toString()))
    if(cursor.moveToFirst()){
        var i = 0
        while(cursor.moveToNext() and (i < 5)){
            map[cursor.getString(0) ?: cursor.getString(1)] = cursor.getInt(2).toFloat()
            i++
        }
    }
    return map
}
fun top5amount(context: Context, runID: Long) : Map<String, Float> {
    val map = HashMap<String, Float>()
    val db = DbHelper.getInstance(context)!!.readableDatabase
    val cursor = db.rawQuery(DbQueries.TOP_5_AMOUNT, arrayOf(runID.toString()))
    var i = 0
    if(cursor.moveToFirst()){
        while(cursor.moveToNext() and (i < 5)){
            map[cursor.getString(0) ?: cursor.getString(1)] = cursor.getInt(2).toFloat()
            i++
        }
    }
    return map
}

fun topNight(context: Context, runID: Long) : Map<String, Float> {
    val map = HashMap<String, Float>()
    val db = DbHelper.getInstance(context)!!.readableDatabase
    val cursor = db.rawQuery(DbQueries.TOP_NIGHT, arrayOf(runID.toString()))
    if(cursor.moveToFirst()){
        var i = 0
        while(cursor.moveToNext()  and (i < 5)){
            map[cursor.getString(0) ?: cursor.getString(1)] = cursor.getInt(2).toFloat()
            i++
        }
    }
    return map
}
fun missedCalls(context: Context, runID: Long) : Map<String, Float>{
    val map = TreeMap<String, Float>()
    val db = DbHelper.getInstance(context)!!.readableDatabase
    val cursor = db.rawQuery(DbQueries.MISSED, arrayOf(runID.toString()))
    var i = 0
    if(cursor.moveToFirst()){
        while(cursor.moveToNext() and (i < 5)){
            map[cursor.getString(0) ?: cursor.getString(1)] = cursor.getInt(2).toFloat()
            i++
        }
    }
    return map
}
fun top3callFactor(context: Context, runID: Long, order: Int = 1) : Map<String, Float>{
    val map = TreeMap<String, Float>()
    val db = DbHelper.getInstance(context)!!.readableDatabase
    var cursor = db.rawQuery(DbQueries.GET_DURATION, arrayOf(runID.toString()))
    if(cursor.moveToFirst()){
        while(cursor.moveToNext()){
            if(cursor.getInt(2).toFloat() > 0){
                map[cursor.getString(0) ?: cursor.getString(1)] = cursor.getInt(2).toFloat()
            }
        }
    }
    cursor = db.rawQuery(DbQueries.TOP_LOGS_AMOUNT, arrayOf(runID.toString()))
    if(cursor.moveToFirst()){
        while(cursor.moveToNext()){
            val duration = map[cursor.getString(0) ?: cursor.getString(1)]
            if (duration != null) map[cursor.getString(0) ?: cursor.getString(1)] = duration / cursor.getInt(2).toFloat()
        }
    }
    var i = 0
    val map3 = TreeMap<String, Float>()
    val sortedMap =
            if (order < 0) map.toList().sortedBy { (_, value) -> value}.reversed().toMap()
            else map.toList().sortedBy { (_, value) -> value}.toMap()
    for (entry in sortedMap){
        if (i == 3) break
        i++
        map3[entry.key] = entry.value
    }
    return map3
}