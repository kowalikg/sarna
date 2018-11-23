package pl.edu.agh.sarna.metadata.asynctask

import android.content.Context
import android.os.Build
import android.provider.CallLog
import android.util.Log
import androidx.annotation.RequiresApi
import pl.edu.agh.sarna.R
import pl.edu.agh.sarna.db.model.calls.CallsLogsInfo
import pl.edu.agh.sarna.db.model.contacts.ContactsInfo
import pl.edu.agh.sarna.db.scripts.*
import pl.edu.agh.sarna.model.SubtaskStatus
import pl.edu.agh.sarna.permissions.checkCallLogsPermission
import pl.edu.agh.sarna.permissions.checkContactsPermission
import pl.edu.agh.sarna.report.ReportEntry
import pl.edu.agh.sarna.report.asynctask.ReportTask
import pl.edu.agh.sarna.utils.kotlin.GraphEntry
import pl.edu.agh.sarna.utils.kotlin.async.AsyncResponse
import pl.edu.agh.sarna.utils.kotlin.isKitKat4_4
import java.lang.ref.WeakReference

class MetadataReportTask(contextReference: WeakReference<Context>, response: AsyncResponse, val runID: Long) : ReportTask(contextReference, response) {
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun doInBackground(vararg p0: Void?): List<ReportEntry>? {
        val metaList = ArrayList<ReportEntry>()

        metaList.addAll(generateContactReport())
        metaList.addAll(generateCallReport())
        return metaList

    }

    private fun generateContactReport(): Collection<ReportEntry> {
        val metaList = ArrayList<ReportEntry>()
        val permitted = contactPermission(contextReference.get()!!, runID)
        val result = if (permitted) contextReference.get()!!.getString(R.string.permission_granted) else contextReference.get()!!.getString(R.string.permission_not_granted)

        metaList.add(ReportEntry(
                "${contextReference.get()!!.getString(R.string.contact_condition)} -> $result"
        ))
        if(permitted){
            val bum = generateContactsReport()
            metaList.add(ReportEntry("${bum.description} : ${bum.value}."))
            if(bum.value as Long > 0) metaList.add(ReportEntry(contextReference.get()!!.getString(R.string.contacts_description)))
            else metaList.add(ReportEntry(contextReference.get()!!.getString(R.string.contacts_not_found)))
        }
        return metaList
    }

    private fun generateCallReport(): Collection<ReportEntry> {
        val metaList = ArrayList<ReportEntry>()
        val permitted = callLogPermission(contextReference.get()!!, runID)
        val result = if (permitted) contextReference.get()!!.getString(R.string.permission_granted) else contextReference.get()!!.getString(R.string.permission_not_granted)

        metaList.add(ReportEntry(
                "${contextReference.get()!!.getString(R.string.log_condition)} -> $result"
        ))

        if (permitted) {
            val bum = generateCallLogsReport()
            metaList.add(ReportEntry("${bum.description} : ${bum.value}."))
            if(bum.value as Long > 0){
                metaList.add(ReportEntry(contextReference.get()!!.getString(R.string.log_analyze_title)))

                val graph = GraphEntry(contextReference.get()!!.getString(R.string.most_frequent_contact), top5amount(contextReference.get()!!, runID))
                val graph2 = GraphEntry(contextReference.get()!!.getString(R.string.long_duration_contact), top5duration(contextReference.get()!!, runID))
                val graph3 = GraphEntry(contextReference.get()!!.getString(R.string.night_contact), topNight(contextReference.get()!!, runID))
                val graph4 = GraphEntry(contextReference.get()!!.getString(R.string.frequent_short_contact), top3callFactor(contextReference.get()!!, runID))
                val graph5 = GraphEntry(contextReference.get()!!.getString(R.string.rare_long_contact), top3callFactor(contextReference.get()!!, runID, -1))
                val graph6 = GraphEntry(contextReference.get()!!.getString(R.string.missed_contact), missedCalls(contextReference.get()!!, runID))
                metaList.add(ReportEntry(contextReference.get()!!.getString(R.string.most_frequent_contact_description), graph))
                metaList.add(ReportEntry(contextReference.get()!!.getString(R.string.long_duration_contact_description), graph2))
                metaList.add(ReportEntry(contextReference.get()!!.getString(R.string.night_contact_description), graph3))
                metaList.add(ReportEntry(contextReference.get()!!.getString(R.string.frequent_short_contact_description), graph4))
                metaList.add(ReportEntry(contextReference.get()!!.getString(R.string.rare_long_contact_description), graph5))
                metaList.add(ReportEntry(contextReference.get()!!.getString(R.string.missed_contact_description), graph6))
            }
            else metaList.add(ReportEntry(contextReference.get()!!.getString(R.string.contacts_not_found)))

        }
        return metaList
    }

    private fun generateContactsReport(): SubtaskStatus {
        return SubtaskStatus(contextReference.get()!!.getString(R.string.contacts_amount), contactsAmount(contextReference.get(), runID))

    }

    private fun generateCallLogsReport(): SubtaskStatus {
        return SubtaskStatus(contextReference.get()!!.getString(R.string.call_logs_amount), callLogsAmount(contextReference.get(), runID))

    }

}
