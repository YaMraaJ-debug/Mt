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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.engine.UrlDetector
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan950
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDownloadDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (url: String, backend: String?, quality: String?, isAudio: Boolean) -> Unit
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var inputUrl by remember { mutableStateOf("") }
    var selectedBackend by remember { mutableStateOf<String?>(null) } // null = Auto
    var selectedQuality by remember { mutableStateOf("AUTO") }
    var isAudioOnly by remember { mutableStateOf(false) }

    val detected = remember(inputUrl) {
        if (inputUrl.isNotBlank()) UrlDetector.detect(inputUrl.lineSequence().firstOrNull() ?: "") else null
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Slate950,
        contentColor = Slate200,
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Cyan400, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ADD DOWNLOAD (dl add)",
                        color = Slate200,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Slate400, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // URL Input
            Text(
                text = "URL OR BATCH LIST (ONE PER LINE):",
                color = Slate400,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = inputUrl,
                onValueChange = { inputUrl = it },
                placeholder = {
                    Text(
                        text = "https://drive.google.com/file/d/... or YouTube / direct link",
                        color = Slate500,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                },
                minLines = 3,
                maxLines = 5,
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
                    .testTag("download_url_input")
            )

            // Auto-detection Pill
            if (detected != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Slate900)
                        .border(1.dp, Cyan400.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "DETECTED: ",
                            color = Slate400,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = detected.backend.uppercase(),
                            color = Cyan400,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = detected.suggestedTitle.take(24),
                        color = Slate400,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Samples Row
            Text(
                text = "QUICK TEMPLATES:",
                color = Slate400,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TemplateChip("G-Drive") {
                    inputUrl = "https://drive.google.com/file/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OIv524/view"
                }
                TemplateChip("YouTube") {
                    inputUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
                }
                TemplateChip("Direct ZIP") {
                    inputUrl = "https://sample-videos.com/zip/10mb.zip"
                }
                TemplateChip("Magnet") {
                    inputUrl = "magnet:?xt=urn:btih:d6b4...&dn=Ubuntu-24.04-Desktop.iso"
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Engine / Backend Selector
            Text(
                text = "ENGINE / RESOLVER:",
                color = Slate400,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                EngineChip("AUTO", selected = selectedBackend == null) { selectedBackend = null }
                EngineChip("G-DRIVE", selected = selectedBackend == "gdrive") { selectedBackend = "gdrive" }
                EngineChip("YT-DLP", selected = selectedBackend == "yt-dlp") { selectedBackend = "yt-dlp" }
                EngineChip("DIRECT", selected = selectedBackend == "direct") { selectedBackend = "direct" }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quality & Audio Extract
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AUDIO EXTRACTION ONLY",
                        color = Slate200,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Extract as high-bitrate MP3 / M4A",
                        color = Slate500,
                        fontSize = 10.sp
                    )
                }

                Switch(
                    checked = isAudioOnly,
                    onCheckedChange = { isAudioOnly = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Cyan400,
                        checkedTrackColor = Cyan950,
                        uncheckedThumbColor = Slate500,
                        uncheckedTrackColor = Slate800
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button
            Button(
                onClick = {
                    if (inputUrl.isNotBlank()) {
                        onSubmit(inputUrl, selectedBackend, selectedQuality, isAudioOnly)
                        onDismiss()
                    }
                },
                enabled = inputUrl.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cyan400,
                    contentColor = Slate950,
                    disabledContainerColor = Slate800,
                    disabledContentColor = Slate500
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("submit_download_button")
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "QUEUE DOWNLOAD",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TemplateChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Slate900)
            .border(1.dp, Slate800, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Slate400,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun EngineChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) Cyan950 else Slate900
    val border = if (selected) Cyan400 else Slate800
    val color = if (selected) Cyan400 else Slate400

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
