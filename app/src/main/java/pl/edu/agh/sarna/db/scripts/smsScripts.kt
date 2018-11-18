package pl.edu.agh.sarna.db.scripts

import android.content.ContentValues
import android.content.Context
import android.database.DatabaseUtils
import android.provider.BaseColumns
import android.util.Log
import pl.edu.agh.sarna.db.DbHelper
import pl.edu.agh.sarna.db.model.smsToken.Codes
import pl.edu.agh.sarna.db.model.smsToken.SmsPermissions
import pl.edu.agh.sarna.db.model.smsToken.TokenSmsDetails
import pl.edu.agh.sarna.permissions.checkReadSmsPermission
import pl.edu.agh.sarna.permissions.checkReceiveSmsPermission
import pl.edu.agh.sarna.permissions.checkSendSmsPermission
import pl.edu.agh.sarna.smsToken.model.SmsMessage
import pl.edu.agh.sarna.smsToken.model.Mode
import pl.edu.agh.sarna.utils.kotlin.isDefaultSmsApp
import pl.edu.agh.sarna.utils.kotlin.toInt
import java.util.*

fun insertTokenQuery(context: Context?, processID: Long, mode: Int) : Long? {
    val dbHelper = DbHelper.getInstance(context!!)
    val db = dbHelper!!.writableDatabase

    val values = ContentValues().apply {
        put(TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_PROCESS_ID, processID)
        put(TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_MODE, mode)
        put(TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_START_TIME, Calendar.getInstance().timeInMillis.toString())
    }

    val runID = db?.insert(TokenSmsDetails.TokenSmsDetailsEntry.TABLE_NAME, null, values)
    Log.i("ID", "New run ID = $runID")

    return runID
}
fun updateTokenMethod(context: Context?, runID: Long, status: Boolean) {
    val db = DbHelper.getInstance(context!!)!!.writableDatabase
    val cv = ContentValues()
    cv.put(TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_END_TIME, Calendar.getInstance().timeInMillis.toString())
    cv.put(TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_STATUS, status.toInt())

    db.update(TokenSmsDetails.TokenSmsDetailsEntry.TABLE_NAME, cv, "${BaseColumns._ID} = ?", arrayOf(runID.toString()));
}
fun insertSmsPermissions(context: Context?, runID: Long) : Long {
    val dbHelper = DbHelper.getInstance(context!!)
    val db = dbHelper!!.writableDatabase

    val values = ContentValues().apply {
        put(SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_RUN_ID, runID)
        put(SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_READ, checkReadSmsPermission(context))
        put(SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_RECEIVE, checkReceiveSmsPermission(context))
        put(SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_SEND, checkSendSmsPermission(context))
        put(SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_DEFAULT_APP, isDefaultSmsApp(context))
    }

    return db?.insert(SmsPermissions.SmsPermissionsEntry.TABLE_NAME, null, values)!!
}
fun getLastRunID(context: Context?) : Long {
    val dbHelper = DbHelper.getInstance(context!!)
    val db = dbHelper!!.writableDatabase
    val cursor = db!!.query(
            TokenSmsDetails.TokenSmsDetailsEntry.TABLE_NAME,
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
fun insertCodes(context: Context?, runID: Long, sms: SmsMessage) : Long {
    val dbHelper = DbHelper.getInstance(context!!)
    val db = dbHelper!!.writableDatabase

    val values = ContentValues().apply {
        put(Codes.CodesEntry.COLUMN_NAME_RUN_ID, runID)
        put(Codes.CodesEntry.COLUMN_NAME_CODE, sms.content)
        put(Codes.CodesEntry.COLUMN_NAME_NUMBER, sms.number)
    }

    return db?.insert(Codes.CodesEntry.TABLE_NAME, null, values)!!
}
fun codesAmount(context: Context?, runID: Long): Long {
    val db = DbHelper.getInstance(context!!)!!.readableDatabase
    return DatabaseUtils.queryNumEntries(db, Codes.CodesEntry.TABLE_NAME,
            "${Codes.CodesEntry.COLUMN_NAME_RUN_ID}=?", arrayOf(runID.toString()))
}
fun smsMethodProceed(context: Context?, runID: Long): Boolean {
    val dbHelper = DbHelper.getInstance(context!!)
    val db = dbHelper!!.writableDatabase
    val cursor = db!!.query(
            TokenSmsDetails.TokenSmsDetailsEntry.TABLE_NAME,
            arrayOf(TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_END_TIME),
            "${BaseColumns._ID}=?",
            arrayOf(runID.toString()),
            null, null,
            "_id DESC" ,
            "1"
    )
    if (cursor.moveToFirst()) {
        return cursor.getString(cursor.getColumnIndex(TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_END_TIME)) != null
    }
    return false
}