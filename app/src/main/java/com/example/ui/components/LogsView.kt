package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.LogEntry
import com.example.engine.LogLevel
import com.example.ui.theme.Amber400
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan950
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Rose400
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun LogsView(
    logs: List<LogEntry>,
    onClearLogs: () -> Unit
) {
    var selectedLevel by remember { mutableStateOf<LogLevel?>(null) }
    val filteredLogs = remember(logs, selectedLevel) {
        if (selectedLevel == null) logs else logs.filter { it.level == selectedLevel }
    }
    val listState = rememberLazyListState()

    LaunchedEffect(filteredLogs.size) {
        if (filteredLogs.isNotEmpty()) {
            listState.animateScrollToItem(filteredLogs.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CONSOLE LOGS (mirror.log)",
                    color = Slate200,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "${filteredLogs.size} lines recorded",
                    color = Slate500,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }

            FilledTonalButton(
                onClick = onClearLogs,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Slate900,
                    contentColor = Slate400
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("clear_logs_button")
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CLEAR",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LogLevelChip("ALL", isSelected = selectedLevel == null, color = Cyan400) { selectedLevel = null }
            LogLevelChip("INFO", isSelected = selectedLevel == LogLevel.INFO, color = Cyan400) { selectedLevel = LogLevel.INFO }
            LogLevelChip("SUCCESS", isSelected = selectedLevel == LogLevel.SUCCESS, color = Emerald400) { selectedLevel = LogLevel.SUCCESS }
            LogLevelChip("WARN", isSelected = selectedLevel == LogLevel.WARN, color = Amber400) { selectedLevel = LogLevel.WARN }
            LogLevelChip("ERROR", isSelected = selectedLevel == LogLevel.ERROR, color = Rose400) { selectedLevel = LogLevel.ERROR }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Logs terminal window
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .height(420.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Slate950)
                .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            if (filteredLogs.isEmpty()) {
                Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No logs recorded", color = Slate500, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            } else {
                LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(filteredLogs) { log ->
                        val levelColor = when (log.level) {
                            LogLevel.INFO -> Cyan400
                            LogLevel.SUCCESS -> Emerald400
                            LogLevel.WARN -> Amber400
                            LogLevel.ERROR -> Rose400
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "[${log.timestamp.takeLast(8)}] ",
                                color = Slate500,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "[${log.level.name}] ",
                                color = levelColor,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = log.message,
                                color = Slate200,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogLevelChip(text: String, isSelected: Boolean, color: Color, onClick: () -> Unit) {
    val bg = if (isSelected) Slate800 else Slate900
    val border = if (isSelected) color else Slate800

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) color else Slate400,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
