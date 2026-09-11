package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.JobCounts
import com.example.ui.theme.Amber400
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan950
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Rose400
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun MetricsBar(
    counts: JobCounts,
    activeFilter: String,
    onFilterClick: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricItem(
            label = "ALL",
            count = counts.active + counts.queued + counts.done + counts.failed + counts.paused,
            isSelected = activeFilter == "all",
            accentColor = Cyan400,
            onClick = { onFilterClick("all") },
            testTag = "filter_all"
        )
        MetricItem(
            label = "ACTIVE",
            count = counts.active,
            isSelected = activeFilter == "active",
            accentColor = Cyan400,
            onClick = { onFilterClick("active") },
            testTag = "filter_active"
        )
        MetricItem(
            label = "QUEUED",
            count = counts.queued,
            isSelected = activeFilter == "queued",
            accentColor = Slate400,
            onClick = { onFilterClick("queued") },
            testTag = "filter_queued"
        )
        MetricItem(
            label = "DONE",
            count = counts.done,
            isSelected = activeFilter == "done",
            accentColor = Emerald400,
            onClick = { onFilterClick("done") },
            testTag = "filter_done"
        )
        MetricItem(
            label = "FAILED",
            count = counts.failed,
            isSelected = activeFilter == "failed",
            accentColor = Rose400,
            onClick = { onFilterClick("failed") },
            testTag = "filter_failed"
        )
        MetricItem(
            label = "PAUSED",
            count = counts.paused,
            isSelected = activeFilter == "paused",
            accentColor = Amber400,
            onClick = { onFilterClick("paused") },
            testTag = "filter_paused"
        )
    }
}

@Composable
private fun MetricItem(
    label: String,
    count: Int,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val bg = if (isSelected) Slate800 else Slate900
    val borderCol = if (isSelected) accentColor else Slate800

    Box(
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                color = if (isSelected) accentColor else Slate400,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = count.toString(),
                color = if (isSelected) Color.White else accentColor,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
