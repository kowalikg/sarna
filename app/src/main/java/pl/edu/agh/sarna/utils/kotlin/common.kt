package pl.edu.agh.sarna.utils.kotlin

import android.annotation.TargetApi
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.provider.Telephony


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
@TargetApi(Build.VERSION_CODES.KITKAT)
fun isDefaultSmsApp(context: Context): Boolean {
    return try {
        context.packageName == Telephony.Sms.getDefaultSmsPackage(context)
    } catch (e: NoSuchMethodError){
        false
    }
}

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}
