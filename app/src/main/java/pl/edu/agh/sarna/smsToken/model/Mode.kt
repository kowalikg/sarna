package pl.edu.agh.sarna.smsToken.model

enum class Mode(val description: String) {
    SAFE("Sms token : safe mode"),
    NOT_SAFE("Sms token: not safe mode"),
    DUMMY("Sms token: dummy mode")
}