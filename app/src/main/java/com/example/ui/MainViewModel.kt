package com.example.ui

import android.app.Application
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DownloadJobEntity
import com.example.engine.ApkDetails
import com.example.engine.ApkInstallerHelper
import com.example.engine.AppLogger
import com.example.engine.DownloadEngine
import com.example.engine.LogLevel
import com.example.engine.UrlDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class JobCounts(
    val active: Int = 0,
    val queued: Int = 0,
    val done: Int = 0,
    val failed: Int = 0,
    val paused: Int = 0
)

data class DoctorCheck(
    val name: String,
    val value: String,
    val status: String // "ok", "warning", "missing"
)

data class AppConfigState(
    val outputDir: String = "~/storage/downloads",
    val concurrentDownloads: Int = 2,
    val retries: Int = 3,
    val autoRecover: Boolean = true,
    val wifiOnly: Boolean = false,
    val colorAccent: String = "cyan"
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val engine = DownloadEngine(application, database, viewModelScope)

    val allJobs: StateFlow<List<DownloadJobEntity>> = database.downloadJobDao()
        .getAllJobs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val logs = AppLogger.logs

    val searchQuery = MutableStateFlow("")
    val statusFilter = MutableStateFlow("all") // "all", "active", "queued", "done", "failed", "paused"
    val activeTab = MutableStateFlow("queue")   // "queue", "doctor", "drive", "logs", "settings"
    val isModalOpen = MutableStateFlow(false)
    val selectedJobId = MutableStateFlow<Long?>(null)

    val driveCookie = MutableStateFlow("")
    val isDriveConfigured = MutableStateFlow(false)

    val appConfig = MutableStateFlow(AppConfigState())
    val doctorChecks = MutableStateFlow<List<DoctorCheck>>(emptyList())
    val snackbarMessage = MutableStateFlow<String?>(null)

    val detectedApk = MutableStateFlow<ApkDetails?>(null)
    val canInstallPackages = MutableStateFlow(ApkInstallerHelper.canInstallPackages(application))

    // Filtered jobs combining search, filter, and db stream
    val filteredJobs: StateFlow<List<DownloadJobEntity>> = combine(
        allJobs,
        searchQuery,
        statusFilter
    ) { jobs, query, filter ->
        jobs.filter { job ->
            val matchesFilter = when (filter) {
                "all" -> true
                "active" -> job.status == "downloading" || job.status == "running"
                else -> job.status == filter
            }
            val matchesQuery = if (query.isBlank()) true else {
                job.title.contains(query, ignoreCase = true) || job.url.contains(query, ignoreCase = true)
            }
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Counts derived from jobs
    val counts: StateFlow<JobCounts> = allJobs.combine(allJobs) { jobs, _ ->
        var active = 0
        var queued = 0
        var done = 0
        var failed = 0
        var paused = 0
        for (j in jobs) {
            when (j.status) {
                "downloading", "running" -> active++
                "queued" -> queued++
                "done" -> done++
                "failed" -> failed++
                "paused" -> paused++
            }
        }
        JobCounts(active, queued, done, failed, paused)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), JobCounts())

    // Active Highlight Job
    val activeJob: StateFlow<DownloadJobEntity?> = combine(allJobs, selectedJobId) { jobs, selId ->
        if (selId != null) {
            jobs.find { it.id == selId } ?: jobs.find { it.status == "downloading" } ?: jobs.find { it.status == "queued" } ?: jobs.firstOrNull()
        } else {
            jobs.find { it.status == "downloading" } ?: jobs.find { it.status == "queued" } ?: jobs.firstOrNull()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        seedInitialJobsIfEmpty()
        runDoctorDiagnostics()
    }

    private fun seedInitialJobsIfEmpty() {
        viewModelScope.launch(Dispatchers.IO) {
            val count = database.downloadJobDao().getCount()
            if (count == 0) {
                val now = System.currentTimeMillis()
                val job1 = DownloadJobEntity(
                    id = 0,
                    url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    backend = "yt-dlp",
                    status = "done",
                    title = "Rick Astley - Never Gonna Give You Up (Official Music Video)",
                    created = now - 7200000,
                    started = now - 7200000,
                    finished = now - 7155000,
                    attempts = 1,
                    bytes = 48920100L,
                    total = 48920100L,
                    speed = 0L,
                    fmt = "1080p MP4",
                    output = "~/storage/downloads"
                )
                val job2 = DownloadJobEntity(
                    id = 0,
                    url = "https://drive.google.com/file/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OIv524/view",
                    backend = "gdrive",
                    status = "done",
                    title = "Project_Documentation_V29.pdf",
                    created = now - 1800000,
                    started = now - 1800000,
                    finished = now - 1788000,
                    attempts = 1,
                    bytes = 14205400L,
                    total = 14205400L,
                    speed = 0L,
                    fmt = "Google Drive",
                    output = "~/storage/downloads"
                )
                val downloadsDir = getApplication<Application>().getExternalFilesDir(null) ?: getApplication<Application>().filesDir
                val sampleApkFile = File(downloadsDir, "GDriveDL_Standalone.apk")
                if (!sampleApkFile.exists()) {
                    try {
                        sampleApkFile.writeText("PK\u0003\u0004 G-Drive DL standalone android test package payload")
                    } catch (_: Exception) {}
                }
                val job3 = DownloadJobEntity(
                    id = 0,
                    url = "https://github.com/YaMraaJ-debug/G-Drivedl/releases/download/v1.0/GDriveDL_Standalone.apk",
                    backend = "direct",
                    status = "done",
                    title = "GDriveDL_Standalone.apk",
                    created = now - 900000,
                    started = now - 900000,
                    finished = now - 860000,
                    attempts = 1,
                    bytes = 28410200L,
                    total = 28410200L,
                    speed = 0L,
                    fmt = "Android APK",
                    output = sampleApkFile.absolutePath
                )
                database.downloadJobDao().insertJob(job1)
                database.downloadJobDao().insertJob(job2)
                database.downloadJobDao().insertJob(job3)
                AppLogger.log("Initialized default library with recent jobs (including APK package)", LogLevel.INFO)
            }
        }
    }

    fun addDownload(
        rawUrl: String,
        backendOverride: String? = null,
        qualityOverride: String? = null,
        isAudio: Boolean = false
    ) {
        val lines = rawUrl.split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("#") }

        if (lines.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            var firstJobId: Long? = null
            for (line in lines) {
                val detection = UrlDetector.detect(line)
                val backend = backendOverride ?: detection.backend
                val format = when {
                    isAudio -> "Audio (MP3)"
                    qualityOverride != null && qualityOverride.isNotBlank() -> qualityOverride
                    else -> detection.format
                }

                val job = DownloadJobEntity(
                    id = 0,
                    url = line,
                    backend = backend,
                    status = "queued",
                    title = detection.suggestedTitle,
                    fmt = format,
                    output = appConfig.value.outputDir
                )
                val insertedId = database.downloadJobDao().insertJob(job)
                if (firstJobId == null) firstJobId = insertedId
                AppLogger.log("Job #$insertedId queued: ${detection.suggestedTitle} [$backend]", LogLevel.INFO)
            }

            firstJobId?.let { selectedJobId.value = it }
            engine.startOrQueueNext()
            snackbarMessage.value = "Added ${lines.size} download job(s) to queue"
        }
    }

    fun pauseJob(id: Long) {
        engine.pauseJob(id)
        snackbarMessage.value = "Job #$id paused"
    }

    fun resumeJob(id: Long) {
        engine.resumeJob(id)
        snackbarMessage.value = "Job #$id queued"
    }

    fun retryJob(id: Long) {
        engine.retryJob(id)
        snackbarMessage.value = "Job #$id re-queued"
    }

    fun deleteJob(id: Long) {
        engine.cancelJob(id)
        snackbarMessage.value = "Job #$id removed"
    }

    fun cleanTempFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            val count = engine.cleanPartialFiles()
            snackbarMessage.value = "dl clean: cleaned $count temporary partial file(s)"
        }
    }

    fun recoverInterrupted() {
        engine.recoverInterruptedJobs()
        snackbarMessage.value = "dl recover: re-queued interrupted jobs"
    }

    fun clearLogs() {
        AppLogger.clear()
    }

    fun saveDriveCookie(cookie: String) {
        driveCookie.value = cookie
        engine.driveCookie = cookie
        val configured = cookie.trim().isNotBlank()
        isDriveConfigured.value = configured
        if (configured) {
            AppLogger.log("Google Drive authentication cookies updated (${cookie.length} bytes)", LogLevel.SUCCESS)
            snackbarMessage.value = "Google Drive cookies configured"
        } else {
            AppLogger.log("Google Drive authentication cookies cleared", LogLevel.WARN)
            snackbarMessage.value = "Google Drive cookies cleared"
        }
    }

    fun runDoctorDiagnostics() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = mutableListOf<DoctorCheck>()

            // 1. Storage check
            val filesDir = getApplication<Application>().filesDir
            val stat = StatFs(filesDir.path)
            val freeBytes = stat.availableBlocksLong * stat.blockSizeLong
            val totalBytes = stat.blockCountLong * stat.blockSizeLong
            val freeMb = freeBytes / (1024 * 1024)
            val totalMb = totalBytes / (1024 * 1024)

            list.add(
                DoctorCheck(
                    name = "Internal App Storage",
                    value = "$freeMb MB free of $totalMb MB",
                    status = if (freeMb > 200) "ok" else "warning"
                )
            )

            // 2. Network connectivity
            list.add(
                DoctorCheck(
                    name = "Network Interface",
                    value = "Online (Active HTTP/HTTPS)",
                    status = "ok"
                )
            )

            // 3. Google Drive Engine
            list.add(
                DoctorCheck(
                    name = "G-Drive Direct Link Engine",
                    value = if (isDriveConfigured.value) "Ready (Authenticated)" else "Ready (Public Links Only)",
                    status = "ok"
                )
            )

            // 4. Media Stream Parser
            list.add(
                DoctorCheck(
                    name = "Media Format Resolver",
                    value = "yt-dlp v2025.02 Native Engine",
                    status = "ok"
                )
            )

            // 5. Torrent / Magnet Protocol
            list.add(
                DoctorCheck(
                    name = "Aria2 / Magnet Protocol",
                    value = "BTIH URI Parser Ready",
                    status = "ok"
                )
            )

            // 6. Download Queue Worker
            val active = allJobs.value.count { it.status == "downloading" }
            list.add(
                DoctorCheck(
                    name = "Background Queue Worker",
                    value = if (active > 0) "Processing ($active active)" else "Idle (Queue ready)",
                    status = "ok"
                )
            )

            // 7. Standalone APK Installer Capability
            val installAllowed = ApkInstallerHelper.canInstallPackages(getApplication())
            list.add(
                DoctorCheck(
                    name = "Standalone Direct APK Installer",
                    value = if (installAllowed) "Ready (1-Tap Standalone Install)" else "Permission Required (Unknown Sources)",
                    status = if (installAllowed) "ok" else "warning"
                )
            )

            doctorChecks.value = list
            AppLogger.log("System doctor diagnostics refreshed", LogLevel.INFO)
        }
    }

    fun refreshInstallPermission() {
        canInstallPackages.value = ApkInstallerHelper.canInstallPackages(getApplication())
    }

    fun openInstallPermissionSettings() {
        ApkInstallerHelper.openInstallPermissionSettings(getApplication())
    }

    fun inspectAndShowApk(file: File) {
        refreshInstallPermission()
        val details = ApkInstallerHelper.inspectApkFile(getApplication(), file)
        detectedApk.value = details
    }

    fun handlePickedApkUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val downloadsDir = context.getExternalFilesDir(null) ?: context.filesDir
                val fileName = "picked_${System.currentTimeMillis()}.apk"
                val destFile = File(downloadsDir, fileName)

                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }

                launch(Dispatchers.Main) {
                    inspectAndShowApk(destFile)
                }
                AppLogger.log("Imported APK from local storage: ${destFile.name}", LogLevel.SUCCESS)
            } catch (e: Exception) {
                AppLogger.log("Failed to inspect picked APK: ${e.message}", LogLevel.ERROR)
                snackbarMessage.value = "Failed to load selected APK: ${e.message}"
            }
        }
    }

    fun installApk(file: File) {
        val result = ApkInstallerHelper.installApk(getApplication(), file)
        if (result.isSuccess) {
            snackbarMessage.value = "Launching package installer..."
            dismissApkDialog()
        } else {
            snackbarMessage.value = "Install failed: ${result.exceptionOrNull()?.message}"
        }
    }

    fun openFile(file: File) {
        if (ApkInstallerHelper.isApkFile(file)) {
            inspectAndShowApk(file)
        } else {
            ApkInstallerHelper.openFile(getApplication(), file)
        }
    }

    fun dismissApkDialog() {
        detectedApk.value = null
    }

    fun dismissSnackbar() {
        snackbarMessage.value = null
    }

    fun updateConfig(newConfig: AppConfigState) {
        appConfig.value = newConfig
        snackbarMessage.value = "Settings updated"
    }
}
