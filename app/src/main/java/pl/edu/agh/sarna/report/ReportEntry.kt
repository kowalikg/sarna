package pl.edu.agh.sarna.report

import pl.edu.agh.sarna.model.SubtaskStatus
import pl.edu.agh.sarna.utils.kotlin.GraphEntry

class ReportEntry(val status: SubtaskStatus?,
                  val description: String = "",
                  val graphList: GraphEntry? = null) {
}