package pl.edu.agh.sarna.smsToken.task

import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.db.DbHelper
import pl.edu.agh.sarna.db.model.smsToken.Codes
import pl.edu.agh.sarna.db.model.smsToken.SmsPermissions
import pl.edu.agh.sarna.db.model.smsToken.TokenSmsDetails
import pl.edu.agh.sarna.db.scripts.codesAmount
import pl.edu.agh.sarna.db.scripts.getCodes
import pl.edu.agh.sarna.db.scripts.getMethodStatus
import pl.edu.agh.sarna.db.scripts.getModeByRunID
import pl.edu.agh.sarna.model.SubtaskStatus
import pl.edu.agh.sarna.report.ReportEntry
import pl.edu.agh.sarna.report.asynctask.ReportTask
import pl.edu.agh.sarna.smsToken.model.Mode
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.toBoolean
import java.lang.StringBuilder
import java.lang.ref.WeakReference


class TokenReportTask(contextReference: WeakReference<Context>, response: AsyncResponse, val runID: Long) : ReportTask(contextReference, response) {
    private val projectionGeneral = arrayOf(
            TokenSmsDetails.TokenSmsDetailsEntry.COLUMN_NAME_STATUS
    )
    private val projectionPermission = arrayOf(
            SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_DEFAULT_APP,
            SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_READ,
            SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_RECEIVE,
            SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_SEND
    )

    override fun doInBackground(vararg p0: Void?): List<ReportEntry>? {
        val list = ArrayList<SubtaskStatus>()
        val reportList = ArrayList<ReportEntry>()
        val mode = getModeByRunID(contextReference.get(), runID)
        if (mode == -1) return skippedMethod()
        return if (mode !in arrayOf(Mode.TEST.ordinal, Mode.TEST_DUMMY.ordinal)) {
            reportList.add(ReportEntry("Informacje na temat testu autoryzacji metody"))
            reportList.add(ReportEntry(contextReference.get()!!.getString(R.string.permission_list)))
            list.addAll(generateTableReport(runID - 1, SmsPermissions.SmsPermissionsEntry.TABLE_NAME, projectionPermission, SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_RUN_ID)!!)
            list.addAll(generateTableModeReport(runID - 1, TokenSmsDetails.TokenSmsDetailsEntry.TABLE_NAME, projectionGeneral)!!)
            list.forEach {
                val emojied = it.toEmoji()
                reportList.add(ReportEntry(emojied.description + ":" + emojied.value))
            }
            list.clear()
            reportList.add(ReportEntry("Informacje na temat właściwej metody"))
            reportList.add(ReportEntry(contextReference.get()!!.getString(R.string.permission_list)))
            list.addAll(generateTableReport(runID, SmsPermissions.SmsPermissionsEntry.TABLE_NAME, projectionPermission, SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_RUN_ID)!!)
            list.addAll(generateTableModeReport(runID, TokenSmsDetails.TokenSmsDetailsEntry.TABLE_NAME, projectionGeneral)!!)

            list.forEach {
                val emojied = it.toEmoji()
                reportList.add(ReportEntry(emojied.description + ":" + emojied.value))
            }
            reportList.add(ReportEntry("Przejęte kody: ${codesAmount(contextReference.get(), runID)}"))
            reportList.addAll(generateCodes())
            reportList
        } else generateTest(mode)
    }

    private fun generateCodes(): ArrayList<ReportEntry> {
        val map = getCodes(contextReference.get(), runID)
        val list = ArrayList<ReportEntry>()
        for ((key, value) in map) {
            list.add(ReportEntry("z numeru: ${key} przechwycono kod: ${value}"))
        }
        return list
    }

    private fun generateTest(mode: Int): List<ReportEntry>? {
        val problem = StringBuilder()
        val reportList = ArrayList<ReportEntry>()
        problem.append(contextReference.get()!!.getString(R.string.method_not_launched) + "\n")
        problem.append(contextReference.get()!!.getString(R.string.reason) + ":\n")

        val report = generateTableReport(runID, SmsPermissions.SmsPermissionsEntry.TABLE_NAME, projectionPermission, SmsPermissions.SmsPermissionsEntry.COLUMN_NAME_RUN_ID)
        report?.let {
            if (getMethodStatus(contextReference.get(), runID).toBoolean()) {
                problem.append(contextReference.get()!!.getString(R.string.test_method_worked) + "\n")
                problem.append(contextReference.get()!!.getString(R.string.method_skipped) + "\n")
            } else {
                problem.append(contextReference.get()!!.getString(R.string.test_method_failed) + "\n")
            }
            problem.append("\n" + contextReference.get()!!.getString(R.string.permission_list))
            reportList.add(ReportEntry(problem.toString()))

            report.forEach { it ->
                val emojied = it.toEmoji()
                reportList.add(ReportEntry(emojied.description + ":" + emojied.value))
            }

        } ?: kotlin.run {
            problem.append(contextReference.get()!!.getString(R.string.test_method_failed) + "\n")
            reportList.add(ReportEntry(problem.toString()))
        }
        return reportList
    }

    private fun generateTableModeReport(runID: Long, tableName: String, projection: Array<String>): ArrayList<SubtaskStatus>? {
        val db = DbHelper.getInstance(contextReference.get())!!.readableDatabase
        val cursor = db.query(
                tableName,
                projection,
                "${BaseColumns._ID}=?",
                arrayOf("$runID"),
                null, null,
                null
        )
        val list = ArrayList<SubtaskStatus>()
        if (cursor.moveToFirst()) {
            list.addAll(generateList(cursor, projection))
        }
        return list
    }

    override fun generateList(cursor: Cursor?, projection: Array<String>): ArrayList<SubtaskStatus> {
        val list = ArrayList<SubtaskStatus>()
        for (task in projection) {
            list.add(SubtaskStatus(
                    task.replace("_", " "),
                    cursor!!.getInt(cursor.getColumnIndex(task)).toBoolean()))
        }
        return list
    }

}
