package pl.edu.agh.sarna.db.model.dirtycow

import android.provider.BaseColumns

object DirtyCowInfo {
    object DirtyCowInfoEntry : BaseColumns {
        const val TABLE_NAME = "DirtyCowInfo"
        const val COLUMN_NAME_RUN_ID = "RunID"
        const val COLUMN_NAME_BUILD = "Build"
        const val COLUMN_NAME_KERNEL = "Kernel"
        const val COLUMN_NAME_VENDOR = "Vendor"
        const val COLUMN_NAME_ETA = "Eta"
        const val COLUMN_NAME_SELINUX = "SELinux"
    }
}