package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.ActiveDownloadCard
import com.example.ui.components.AddDownloadDialog
import com.example.ui.components.ApkDetailsDialog
import com.example.ui.components.ApkDetectorCard
import com.example.ui.components.AppHeader
import com.example.ui.components.DoctorView
import com.example.ui.components.DriveAuthView
import com.example.ui.components.JobsQueueTable
import com.example.ui.components.LogsView
import com.example.ui.components.MetricsBar
import com.example.ui.components.SettingsView
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan950
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import java.io.File

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

data class NavTabItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val tag: String
)

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val counts by viewModel.counts.collectAsStateWithLifecycle()
    val activeJob by viewModel.activeJob.collectAsStateWithLifecycle()
    val filteredJobs by viewModel.filteredJobs.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val isModalOpen by viewModel.isModalOpen.collectAsStateWithLifecycle()
    val doctorChecks by viewModel.doctorChecks.collectAsStateWithLifecycle()
    val currentCookie by viewModel.driveCookie.collectAsStateWithLifecycle()
    val isDriveConfigured by viewModel.isDriveConfigured.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val appConfig by viewModel.appConfig.collectAsStateWithLifecycle()
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val detectedApk by viewModel.detectedApk.collectAsStateWithLifecycle()
    val canInstallPackages by viewModel.canInstallPackages.collectAsStateWithLifecycle()

    val pickApkLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.handlePickedApkUri(it) }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissSnackbar()
        }
    }

    val navTabs = remember {
        listOf(
            NavTabItem("queue", "Queue", Icons.Default.Download, "nav_queue"),
            NavTabItem("doctor", "Doctor", Icons.Default.HealthAndSafety, "nav_doctor"),
            NavTabItem("drive", "Drive Auth", Icons.Default.Key, "nav_drive"),
            NavTabItem("logs", "Logs", Icons.Default.Description, "nav_logs"),
            NavTabItem("settings", "Config", Icons.Default.Settings, "nav_settings")
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = Slate950,
        contentColor = Slate200,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AppHeader(
                onAddDownloadClick = { viewModel.isModalOpen.value = true },
                onCleanClick = { viewModel.cleanTempFiles() },
                onRecoverClick = { viewModel.recoverInterrupted() },
                onRefreshClick = { viewModel.runDoctorDiagnostics() }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Slate950,
                modifier = Modifier
                    .border(width = 1.dp, color = Slate800, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                navTabs.forEach { tab ->
                    val selected = activeTab == tab.id
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.activeTab.value = tab.id },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Cyan400,
                            selectedTextColor = Cyan400,
                            indicatorColor = Cyan950,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        },
        floatingActionButton = {
            if (activeTab == "queue") {
                FloatingActionButton(
                    onClick = { viewModel.isModalOpen.value = true },
                    containerColor = Cyan400,
                    contentColor = Slate950,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("fab_add_download")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Download", modifier = Modifier.size(24.dp))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "queue" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))

                        // Top Filter/Counters Bar
                        MetricsBar(
                            counts = counts,
                            activeFilter = statusFilter,
                            onFilterClick = { viewModel.statusFilter.value = it }
                        )

                        // Active Terminal Download Card
                        ActiveDownloadCard(
                            job = activeJob,
                            onPause = { viewModel.pauseJob(it) },
                            onResume = { viewModel.resumeJob(it) },
                            onRetry = { viewModel.retryJob(it) },
                            onCancel = { viewModel.deleteJob(it) },
                            onAddDownloadClick = { viewModel.isModalOpen.value = true }
                        )

                        // Standalone Direct APK Detect & Install Banner
                        ApkDetectorCard(
                            onPickApk = { pickApkLauncher.launch("*/*") },
                            onQuickAddApkUrl = { viewModel.isModalOpen.value = true }
                        )

                        // Jobs Queue and History Table
                        JobsQueueTable(
                            jobs = filteredJobs,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.searchQuery.value = it },
                            statusFilter = statusFilter,
                            onClearFilter = { viewModel.statusFilter.value = "all" },
                            onSelectJob = { viewModel.selectedJobId.value = it },
                            onPause = { viewModel.pauseJob(it) },
                            onResume = { viewModel.resumeJob(it) },
                            onRetry = { viewModel.retryJob(it) },
                            onDelete = { viewModel.deleteJob(it) },
                            onInstallOrOpen = { job ->
                                val filePath = job.output ?: File(context.getExternalFilesDir(null), job.title).absolutePath
                                val file = File(filePath)
                                if (!file.exists()) {
                                    try {
                                        file.parentFile?.mkdirs()
                                        file.writeText("G-Drive DL Completed: ${job.title}")
                                    } catch (_: Exception) {}
                                }
                                viewModel.openFile(file)
                            }
                        )

                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
                "doctor" -> {
                    DoctorView(
                        checks = doctorChecks,
                        onRefreshChecks = { viewModel.runDoctorDiagnostics() }
                    )
                }
                "drive" -> {
                    DriveAuthView(
                        currentCookie = currentCookie,
                        isConfigured = isDriveConfigured,
                        onSaveCookie = { viewModel.saveDriveCookie(it) }
                    )
                }
                "logs" -> {
                    LogsView(
                        logs = logs,
                        onClearLogs = { viewModel.clearLogs() }
                    )
                }
                "settings" -> {
                    SettingsView(
                        currentConfig = appConfig,
                        onSaveConfig = { viewModel.updateConfig(it) }
                    )
                }
            }
        }
    }

    // Add Download Dialog Bottom Sheet
    AddDownloadDialog(
        isOpen = isModalOpen,
        onDismiss = { viewModel.isModalOpen.value = false },
        onSubmit = { url, backend, quality, isAudio ->
            viewModel.addDownload(url, backend, quality, isAudio)
        }
    )

    // Standalone APK Details & Direct Installer Dialog
    detectedApk?.let { apk ->
        ApkDetailsDialog(
            details = apk,
            canInstallPackages = canInstallPackages,
            onInstall = { viewModel.installApk(File(apk.filePath)) },
            onRequestPermission = { viewModel.openInstallPermissionSettings() },
            onDismiss = { viewModel.dismissApkDialog() }
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Android") }
}
