package pl.edu.agh.sarna.db.scripts

import android.content.ContentValues
import android.content.Context
import android.provider.BaseColumns
import android.util.Log
import pl.edu.agh.sarna.db.DbHelper
import pl.edu.agh.sarna.db.model.dirtycow.DirtyCowDetails
import pl.edu.agh.sarna.db.model.dirtycow.DirtyCowInfo
import pl.edu.agh.sarna.utils.kotlin.toInt
import java.util.*


fun insertDirtyCowQuery(context: Context?, processID: Long) : Long? {
    val dbHelper = DbHelper.getInstance(context)
    val db = dbHelper!!.writableDatabase

    val values = ContentValues().apply {
        put(DirtyCowDetails.DirtyCowDetailsEntry.COLUMN_NAME_PROCESS_ID, processID)
        put(DirtyCowDetails.DirtyCowDetailsEntry.COLUMN_NAME_START_TIME, Calendar.getInstance().timeInMillis.toString())
    }

    val runID = db?.insert(DirtyCowDetails.DirtyCowDetailsEntry.TABLE_NAME, null, values)
    Log.i("ID", "New run ID = $runID")

    return runID
}

fun insertDirtyCowInfo(context: Context?,
                       runID: Long,
                       eta: Long,
                       vendor: String,
                       build: String,
                       SELinux: Boolean,
                       kernel: String
                       ) {
    val dbHelper = DbHelper.getInstance(context)
    val db = dbHelper!!.writableDatabase
    val values = ContentValues().apply {
        put(DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_RUN_ID, runID)
        put(DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_ETA, eta)
        put(DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_KERNEL, kernel)
        put(DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_VENDOR, vendor)
        put(DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_BUILD, build)
        put(DirtyCowInfo.DirtyCowInfoEntry.COLUMN_NAME_SELINUX, SELinux)
    }

    val tryID = db?.insert(DirtyCowInfo.DirtyCowInfoEntry.TABLE_NAME, null, values)
    Log.i("ID", "New runutils ID = $tryID")
}
fun updateDirtyCowMethod(context: Context?, runID: Long, status: Boolean) {
    val db = DbHelper.getInstance(context)!!.writableDatabase
    val cv = ContentValues()
    cv.put(DirtyCowDetails.DirtyCowDetailsEntry.COLUMN_NAME_END_TIME, Calendar.getInstance().timeInMillis.toString())
    cv.put(DirtyCowDetails.DirtyCowDetailsEntry.COLUMN_NAME_STATUS, status.toInt())

    db.update(DirtyCowDetails.DirtyCowDetailsEntry.TABLE_NAME, cv, "${BaseColumns._ID} = ?", arrayOf(runID.toString()));
}