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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppConfigState
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan950
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun SettingsView(
    currentConfig: AppConfigState,
    onSaveConfig: (AppConfigState) -> Unit
) {
    var outputDir by remember(currentConfig) { mutableStateOf(currentConfig.outputDir) }
    var concurrent by remember(currentConfig) { mutableIntStateOf(currentConfig.concurrentDownloads) }
    var retries by remember(currentConfig) { mutableIntStateOf(currentConfig.retries) }
    var autoRecover by remember(currentConfig) { mutableStateOf(currentConfig.autoRecover) }
    var wifiOnly by remember(currentConfig) { mutableStateOf(currentConfig.wifiOnly) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = Cyan400, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "DOWNLOADER CONFIG (config.json)",
                    color = Slate200,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Engine limits, concurrency, and directory destinations",
                    color = Slate500,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Output directory
        Text(
            text = "OUTPUT DIRECTORY:",
            color = Slate400,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = outputDir,
            onValueChange = { outputDir = it },
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
                .testTag("output_dir_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Max Concurrent Downloads
        Text(
            text = "CONCURRENT DOWNLOAD SLOTS:",
            color = Slate400,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1, 2, 3, 4).forEach { count ->
                NumberSelectorChip(
                    label = "$count Streams",
                    isSelected = concurrent == count,
                    onClick = { concurrent = count }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Retry attempts
        Text(
            text = "AUTO-RETRY ATTEMPTS ON FAILURE:",
            color = Slate400,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1, 2, 3, 5).forEach { count ->
                NumberSelectorChip(
                    label = "$count Tries",
                    isSelected = retries == count,
                    onClick = { retries = count }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Switches
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Slate900)
                .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AUTO-RECOVER QUEUE",
                            color = Slate200,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Automatically resume interrupted jobs on app restart",
                            color = Slate500,
                            fontSize = 10.sp
                        )
                    }

                    Switch(
                        checked = autoRecover,
                        onCheckedChange = { autoRecover = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Cyan400,
                            checkedTrackColor = Cyan950,
                            uncheckedThumbColor = Slate500,
                            uncheckedTrackColor = Slate800
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DOWNLOAD OVER WI-FI ONLY",
                            color = Slate200,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pause downloads when using cellular data",
                            color = Slate500,
                            fontSize = 10.sp
                        )
                    }

                    Switch(
                        checked = wifiOnly,
                        onCheckedChange = { wifiOnly = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Cyan400,
                            checkedTrackColor = Cyan950,
                            uncheckedThumbColor = Slate500,
                            uncheckedTrackColor = Slate800
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Open Source APK Direct Install Instructions Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Slate900)
                .border(1.dp, com.example.ui.theme.Emerald400.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            Text(
                text = "DIRECT APK INSTALLATION GUIDE",
                color = com.example.ui.theme.Emerald400,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "1. AI Studio ke top bar me Settings (⚙️ ya 3 dots) par tap karein.\n" +
                       "2. 'Download APK' select karein (USB Debugging / PC cable ki zaroorat nahi hai).\n" +
                       "3. Phone ke Downloads folder se APK par tap karke 'Install' dabayein.\n" +
                       "4. Bas 'Install unknown apps' permission allow karein.",
                color = Slate200,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                lineHeight = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button
        Button(
            onClick = {
                onSaveConfig(
                    currentConfig.copy(
                        outputDir = outputDir,
                        concurrentDownloads = concurrent,
                        retries = retries,
                        autoRecover = autoRecover,
                        wifiOnly = wifiOnly
                    )
                )
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Cyan400,
                contentColor = Slate950
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("save_config_button")
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "APPLY CONFIGURATION",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun NumberSelectorChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val bg = if (isSelected) Cyan950 else Slate900
    val border = if (isSelected) Cyan400 else Slate800
    val color = if (isSelected) Cyan400 else Slate400

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
