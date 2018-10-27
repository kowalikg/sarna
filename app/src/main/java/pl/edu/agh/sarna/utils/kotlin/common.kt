package pl.edu.agh.sarna.utils.kotlin

import android.os.Build

fun Boolean.toInt() = if (this) 1 else 0
fun Int.toBoolean() = this != 0
fun isOreo8_1(): Boolean {
    return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1
}
fun isOreo8_0(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
}
fun isKitKat4_4(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
}