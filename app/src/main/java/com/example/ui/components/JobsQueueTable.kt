package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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

@Composable
fun JobsQueueTable(
    jobs: List<DownloadJobEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    statusFilter: String,
    onClearFilter: () -> Unit,
    onSelectJob: (Long) -> Unit,
    onPause: (Long) -> Unit,
    onResume: (Long) -> Unit,
    onRetry: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onInstallOrOpen: (DownloadJobEntity) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Section Header & Search bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DOWNLOAD JOBS & QUEUE",
                    color = Slate200,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "${jobs.size} items recorded",
                    color = Slate500,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }

            if (statusFilter != "all") {
                Text(
                    text = "Filter: $statusFilter [Clear]",
                    color = Cyan400,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onClearFilter() }
                        .testTag("clear_filter_button")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = "Search by title or URL...",
                    color = Slate500,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Slate500,
                    modifier = Modifier.size(16.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear search", tint = Slate400, modifier = Modifier.size(14.dp))
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Slate900,
                unfocusedContainerColor = Slate900,
                focusedBorderColor = Cyan400,
                unfocusedBorderColor = Slate800,
                focusedTextColor = Slate200,
                unfocusedTextColor = Slate200
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("search_jobs_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (jobs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate900)
                    .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotBlank()) "No downloads matching '$searchQuery'" else "No downloads in this view",
                    color = Slate500,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                jobs.forEach { job ->
                    JobRowItem(
                        job = job,
                        onClick = { onSelectJob(job.id) },
                        onPause = { onPause(job.id) },
                        onResume = { onResume(job.id) },
                        onRetry = { onRetry(job.id) },
                        onDelete = { onDelete(job.id) },
                        onInstallOrOpen = { onInstallOrOpen(job) }
                    )
                }
            }
        }
    }
}

@Composable
fun JobRowItem(
    job: DownloadJobEntity,
    onClick: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRetry: () -> Unit,
    onDelete: () -> Unit,
    onInstallOrOpen: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Slate900)
            .border(
                1.dp,
                if (job.status == "downloading") Cyan400.copy(alpha = 0.4f) else Slate800,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
            .testTag("job_item_${job.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // ID Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Slate800)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "#${job.id}",
                            color = Slate400,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    BackendBadge(backend = job.backend, format = job.fmt)
                }

                // Status Pill
                StatusBadge(status = job.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title
            Text(
                text = job.title,
                color = Slate200,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // URL
            Text(
                text = job.url,
                color = Slate500,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Progress bar if active or paused
            if (job.status == "downloading" || job.status == "paused") {
                Spacer(modifier = Modifier.height(6.dp))
                val p = if (job.total > 0L) (job.bytes.toFloat() / job.total.toFloat()).coerceIn(0f, 1f) else 0f
                LinearProgressIndicator(
                    progress = { p },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (job.status == "downloading") Cyan400 else Amber400,
                    trackColor = Slate800
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Footer row: size, speed or error, and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatBytes(job.bytes)} / ${formatBytes(job.total)}",
                    color = Slate400,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (job.status == "done") {
                        val isApk = job.title.endsWith(".apk", ignoreCase = true) || job.output?.endsWith(".apk", ignoreCase = true) == true
                        Button(
                            onClick = onInstallOrOpen,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isApk) Emerald400 else Cyan400,
                                contentColor = Slate950
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(26.dp)
                                .testTag("btn_install_job_${job.id}"),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isApk) Icons.Default.Android else Icons.Default.Launch,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isApk) "INSTALL" else "OPEN",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (job.status == "downloading") {
                        IconButton(onClick = onPause, modifier = Modifier.size(26.dp)) {
                            Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause", tint = Amber400, modifier = Modifier.size(14.dp))
                        }
                    } else if (job.status == "paused" || job.status == "queued") {
                        IconButton(onClick = onResume, modifier = Modifier.size(26.dp)) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Resume", tint = Cyan400, modifier = Modifier.size(14.dp))
                        }
                    }

                    IconButton(onClick = onRetry, modifier = Modifier.size(26.dp)) {
                        Icon(imageVector = Icons.Default.Replay, contentDescription = "Retry", tint = Slate400, modifier = Modifier.size(14.dp))
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Delete", tint = Rose400, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (text, color, bg) = when (status) {
        "downloading", "running" -> Triple("RUNNING", Cyan400, Cyan950)
        "queued" -> Triple("QUEUED", Slate300Text(), Slate800)
        "done" -> Triple("COMPLETED", Emerald400, Color(0xFF022C22))
        "failed" -> Triple("FAILED", Rose400, Color(0xFF4C0519))
        "paused" -> Triple("PAUSED", Amber400, Color(0xFF451A03))
        else -> Triple(status.uppercase(), Slate400, Slate800)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(0.5.dp, color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 1.5.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
