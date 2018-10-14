package pl.edu.agh.sarna.values

class WifiLogsValues {
  val wifiFileToNougat = "wpa_supplicant.conf"
    get() = field
  val wifiFileFromOreo = "WifiConfigStore.xml"
    get() = field
  val cmd = "cat /data/misc/wifi/"
    get() = field
  val logFile = "/storage/emulated/0/bom.txt"
    get() = field
}