package com.example.outofroutebuddy.data.archive

import android.content.Context
import java.io.File

object SharedPoolExportStorage {
    fun resolveExportDirectory(context: Context): File {
        val preferred = context.getExternalFilesDir("shared_pool_exports")
        val exportDir = preferred ?: File(context.filesDir, "shared_pool_exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        return exportDir
    }
}
