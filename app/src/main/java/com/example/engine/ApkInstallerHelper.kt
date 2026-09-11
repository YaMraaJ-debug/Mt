package com.example.engine

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File

data class ApkDetails(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val fileSizeBytes: Long,
    val filePath: String,
    val isValid: Boolean,
    val errorMessage: String? = null
)

object ApkInstallerHelper {

    fun isApkFile(file: File): Boolean {
        return file.exists() && file.isFile && file.name.endsWith(".apk", ignoreCase = true)
    }

    fun isApkPath(path: String?): Boolean {
        if (path == null) return false
        return path.endsWith(".apk", ignoreCase = true)
    }

    fun canInstallPackages(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun openInstallPermissionSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                AppLogger.log("Failed to open unknown app sources settings: ${e.message}", LogLevel.ERROR)
            }
        }
    }

    fun inspectApkFile(context: Context, file: File): ApkDetails {
        if (!file.exists()) {
            return ApkDetails(
                appName = file.name,
                packageName = "unknown",
                versionName = "-",
                versionCode = 0L,
                fileSizeBytes = 0L,
                filePath = file.absolutePath,
                isValid = false,
                errorMessage = "File does not exist"
            )
        }

        return try {
            val pm = context.packageManager
            val packageInfo = pm.getPackageArchiveInfo(file.absolutePath, 0)
            if (packageInfo != null) {
                val appInfo = packageInfo.applicationInfo
                val appName = if (appInfo != null) {
                    appInfo.sourceDir = file.absolutePath
                    appInfo.publicSourceDir = file.absolutePath
                    try {
                        pm.getApplicationLabel(appInfo).toString()
                    } catch (e: Exception) {
                        file.nameWithoutExtension
                    }
                } else {
                    file.nameWithoutExtension
                }

                @Suppress("DEPRECATION")
                val vCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    packageInfo.versionCode.toLong()
                }

                ApkDetails(
                    appName = if (appName.isNotBlank()) appName else file.nameWithoutExtension,
                    packageName = packageInfo.packageName ?: "com.app",
                    versionName = packageInfo.versionName ?: "1.0",
                    versionCode = vCode,
                    fileSizeBytes = file.length(),
                    filePath = file.absolutePath,
                    isValid = true
                )
            } else {
                // If package manager couldn't parse archive headers
                ApkDetails(
                    appName = file.nameWithoutExtension,
                    packageName = "android.package",
                    versionName = "1.0",
                    versionCode = 1L,
                    fileSizeBytes = file.length(),
                    filePath = file.absolutePath,
                    isValid = true
                )
            }
        } catch (e: Exception) {
            ApkDetails(
                appName = file.nameWithoutExtension,
                packageName = "unknown",
                versionName = "-",
                versionCode = 0L,
                fileSizeBytes = file.length(),
                filePath = file.absolutePath,
                isValid = false,
                errorMessage = e.message
            )
        }
    }

    fun installApk(context: Context, file: File): Result<Boolean> {
        return try {
            if (!file.exists()) {
                return Result.failure(IllegalArgumentException("File not found: ${file.absolutePath}"))
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
            AppLogger.log("Initiated direct package installation: ${file.name}", LogLevel.SUCCESS)
            Result.success(true)
        } catch (e: Exception) {
            AppLogger.log("Installation intent failed: ${e.message}", LogLevel.ERROR)
            Result.failure(e)
        }
    }

    fun openFile(context: Context, file: File) {
        if (isApkFile(file)) {
            installApk(context, file)
            return
        }

        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val ext = file.extension.lowercase()
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "*/*"

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Open with...").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            AppLogger.log("Failed to open file: ${e.message}", LogLevel.ERROR)
        }
    }
}
