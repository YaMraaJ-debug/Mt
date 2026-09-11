package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DownloadJobEntity
import com.example.ui.theme.Amber400
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan950
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Rose400
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import java.util.Locale

@Composable
fun ActiveDownloadCard(
    job: DownloadJobEntity?,
    onPause: (Long) -> Unit,
    onResume: (Long) -> Unit,
    onRetry: (Long) -> Unit,
    onCancel: (Long) -> Unit,
    onAddDownloadClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Slate900)
            .border(1.dp, if (job?.status == "downloading") Cyan400.copy(alpha = 0.6f) else Slate800, RoundedCornerShape(12.dp))
    ) {
        if (job == null) {
            // Idle Terminal Display
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = Slate500,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "NO ACTIVE STREAM IN PROGRESS",
                    color = Slate400,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Queue is idle. Paste any URL (GDrive, Media, Direct, Torrent) to begin.",
                    color = Slate500,
                    fontSize = 11.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))
                FilledTonalButton(
                    onClick = onAddDownloadClick,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Cyan950,
                        contentColor = Cyan400
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("terminal_add_download_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "START NEW DOWNLOAD",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Active Download Terminal Card
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                // Top terminal status bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (job.status) {
                                        "downloading" -> Cyan400
                                        "done" -> Emerald400
                                        "failed" -> Rose400
                                        "paused" -> Amber400
                                        else -> Slate400
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$ dl status --job=${job.id}",
                            color = Slate400,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }

                    // Backend badge
                    BackendBadge(backend = job.backend, format = job.fmt)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title
                Text(
                    text = job.title,
                    color = Slate200,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Subtitle / URL
                Text(
                    text = job.url,
                    color = Slate500,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar & Percentage
                val progress = if (job.total > 0L) {
                    (job.bytes.toFloat() / job.total.toFloat()).coerceIn(0f, 1f)
                } else 0f
                val percent = (progress * 100).toInt()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Terminal ASCII block bar
                    val filledBlocks = (progress * 16).toInt().coerceIn(0, 16)
                    val emptyBlocks = 16 - filledBlocks
                    val asciiBar = "[${"█".repeat(filledBlocks)}${"░".repeat(emptyBlocks)}]"

                    Text(
                        text = "$asciiBar $percent%",
                        color = Cyan400,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Transfer speed
                    val speedStr = formatSpeed(job.speed)
                    Text(
                        text = if (job.status == "downloading") speedStr else job.status.uppercase(),
                        color = if (job.status == "downloading") Emerald400 else Slate400,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = when (job.status) {
                        "downloading" -> Cyan400
                        "done" -> Emerald400
                        "failed" -> Rose400
                        "paused" -> Amber400
                        else -> Slate500
                    },
                    trackColor = Slate800
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Stats: Transferred / Total, ETA, Attempts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${formatBytes(job.bytes)} / ${formatBytes(job.total)}",
                        color = Slate400,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )

                    val etaStr = calculateEta(job.bytes, job.total, job.speed)
                    Text(
                        text = if (job.status == "downloading") "ETA $etaStr" else "TRIES ${job.attempts}",
                        color = Slate400,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Control Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (job.status == "downloading") {
                        IconButton(
                            onClick = { onPause(job.id) },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Slate800)
                                .testTag("pause_job_button")
                        ) {
                            Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause", tint = Amber400, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    } else if (job.status == "paused" || job.status == "queued") {
                        IconButton(
                            onClick = { onResume(job.id) },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Slate800)
                                .testTag("resume_job_button")
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Resume", tint = Cyan400, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    IconButton(
                        onClick = { onRetry(job.id) },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Slate800)
                            .testTag("retry_job_button")
                    ) {
                        Icon(imageVector = Icons.Default.Replay, contentDescription = "Retry", tint = Slate300Text(), modifier = Modifier.size(16.dp))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { onCancel(job.id) },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Slate800)
                            .testTag("delete_job_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", tint = Rose400, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BackendBadge(backend: String, format: String?) {
    val (label, color, bg) = when (backend.lowercase()) {
        "gdrive" -> Triple("G-DRIVE", Cyan400, Cyan950)
        "yt-dlp" -> Triple(format ?: "YT-DLP", Emerald400, Color(0xFF064E3B))
        "aria2" -> Triple("TORRENT", Amber400, Color(0xFF451A03))
        "gallery" -> Triple("GALLERY", Color(0xFFC084FC), Color(0xFF3B0764))
        else -> Triple("DIRECT", Slate200, Slate800)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(0.5.dp, color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format(Locale.US, "%.1f GB", gb)
        mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
        kb >= 1.0 -> String.format(Locale.US, "%.0f KB", kb)
        else -> "$bytes B"
    }
}

fun formatSpeed(bytesPerSec: Long): String {
    if (bytesPerSec <= 0L) return "0 KB/s"
    val kb = bytesPerSec / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        String.format(Locale.US, "%.1f MB/s", mb)
    } else {
        String.format(Locale.US, "%.0f KB/s", kb)
    }
}

fun calculateEta(current: Long, total: Long, speed: Long): String {
    if (speed <= 0L || total <= current) return "--:--"
    val remainingBytes = total - current
    val seconds = (remainingBytes / speed).toInt()
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.US, "%02d:%02d", mins, secs)
}

@Composable
fun Slate300Text() = Slate200
