package pl.edu.agh.sarna.utils.kotlin

import android.graphics.Color
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

class PieDrawer(private val pieChart: PieChart) {
    private val pieData = ArrayList<PieEntry>()

    fun setData(data: Map<String, Float>){
        for (entry in data){
            pieData.add(PieEntry(entry.value, entry.key))
        }
    }
    fun generate(){
        pieChart.minimumHeight = 500
        pieChart.extraBottomOffset = (-5).toFloat()
        pieChart.extraTopOffset = (-5).toFloat()

        val dataSet = PieDataSet(
                pieData.toMutableList(),"")

        generateLegend()
        dataSet.colors = generateColors()

        val data = PieData(dataSet)
        pieChart.data = data

    }
    private fun generateColors(): java.util.ArrayList<Int> {
        val colors = java.util.ArrayList<Int>()

        for (c in ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c)

        for (c in ColorTemplate.JOYFUL_COLORS)
            colors.add(c)

        for (c in ColorTemplate.COLORFUL_COLORS)
            colors.add(c)

        for (c in ColorTemplate.LIBERTY_COLORS)
            colors.add(c)

        for (c in ColorTemplate.PASTEL_COLORS)
            colors.add(c)

        colors.add(ColorTemplate.getHoloBlue())
        return colors
    }

    private fun generateLegend() {
        val l = pieChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.isWordWrapEnabled = true
        l.setDrawInside(false)
        l.yOffset = 5f

        pieChart.description.isEnabled = false
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(12f)
    }
}