package pl.edu.agh.sarna.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.annotation.NonNull
import android.support.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
fun checkStoragePermission(context: Context) : Boolean {
    return context.checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
}
fun checkLocationPermision(context: Context) : Boolean {
    return context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
}
fun hasAllPermissionsGranted(@NonNull grantResults: IntArray) : Boolean {
    for (grantResult in grantResults) {
        if (grantResult == PackageManager.PERMISSION_DENIED) {
            return false;
        }
    }
    return true;
}