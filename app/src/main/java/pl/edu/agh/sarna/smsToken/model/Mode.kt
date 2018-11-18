package pl.edu.agh.sarna.smsToken.model

enum class Mode(val description: String) {
    DUMMY("Sms token: dummy mode"),
    NOT_SAFE("Sms token: not safe mode"),
    TEST("Test if sms can be sent"),
    TEST_DUMMY("Test if sms can be sent in dummy mode"),
}