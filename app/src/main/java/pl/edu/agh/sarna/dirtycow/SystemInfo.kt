package pl.edu.agh.sarna.dirtycow

import java.io.*
import java.util.regex.Pattern

class SystemInfo {
    var buildDate = "--"
    var vendor = "Unknown"
    var kernelVersion = "x.y.z-none"
    var isSELinuxInstalled = false

    fun launch() {
        val buildDatePattern = "(\\w{0,4} \\w{3,4} \\d{2} \\d{2}:\\d{2}:\\d{2} \\d{4}|\\w{3,4} \\d{2} \\d{2}:\\d{2}:\\d{2} \\w{0,4} \\d{4})"
        isSELinuxInstalled = File("/system/bin/getenforce").exists()

        try {
            val reader = BufferedReader(FileReader(File("/proc/version")) as Reader?)
            val kernelInfo = reader.readLine()
            kernelVersion = kernelInfo.split(" ")[2]
            val matcher = Pattern.compile(buildDatePattern).matcher(kernelInfo)
            if (matcher.find()) {
                buildDate = matcher.group()
            }
        } catch (e: Exception) {
        }

        try {
            val pb = ProcessBuilder("/system/bin/getprop").start()
            val output = BufferedReader(InputStreamReader(pb.inputStream))

            output.forEachLine { l ->
                if (l.startsWith("[ro.product.manufacturer]") or l.startsWith("[ro.product.model]"))
                    vendor += l.split(":")[1] + " "
            }
            output.close()
            pb.destroy()
        } catch (e: Exception) {
        }
    }
}