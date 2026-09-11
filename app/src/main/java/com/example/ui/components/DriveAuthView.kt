package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
fun DriveAuthView(
    currentCookie: String,
    isConfigured: Boolean,
    onSaveCookie: (String) -> Unit
) {
    var cookieText by remember(currentCookie) { mutableStateOf(currentCookie) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Section title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = Cyan400, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "GOOGLE DRIVE AUTH (cookies.txt)",
                    color = Slate200,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Bypass download quotas and access restricted shared Drive files",
                    color = Slate500,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Auth Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Slate900)
                .border(
                    1.dp,
                    if (isConfigured) Emerald400.copy(alpha = 0.5f) else Amber400.copy(alpha = 0.5f),
                    RoundedCornerShape(8.dp)
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "STATUS: ${if (isConfigured) "AUTHENTICATION ACTIVE" else "NOT CONFIGURED"}",
                        color = if (isConfigured) Emerald400 else Amber400,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = if (isConfigured) "${currentCookie.length} bytes loaded into cookie store" else "Public Drive links will download without cookies",
                        color = Slate400,
                        fontSize = 10.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isConfigured) Color(0xFF022C22) else Color(0xFF451A03))
                        .border(0.5.dp, if (isConfigured) Emerald400 else Amber400, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isConfigured) "READY" else "PUBLIC ONLY",
                        color = if (isConfigured) Emerald400 else Amber400,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cookie text input
        Text(
            text = "PASTE COOKIES CONTENT (Netscape or Header string):",
            color = Slate400,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = cookieText,
            onValueChange = { cookieText = it },
            placeholder = {
                Text(
                    text = "# Netscape HTTP Cookie File\n.google.com\tTRUE\t/\tTRUE\t...\tSID\t...\nOR paste: SID=...; HSID=...; SSID=...",
                    color = Slate500,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            },
            minLines = 6,
            maxLines = 10,
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
                .testTag("drive_cookie_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onSaveCookie(cookieText) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cyan400,
                    contentColor = Slate950
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("save_drive_cookie_button")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "SAVE COOKIES", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            OutlinedButton(
                onClick = {
                    cookieText = ""
                    onSaveCookie("")
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Rose400
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(42.dp)
                    .testTag("clear_drive_cookie_button")
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "CLEAR", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Helpful guide box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Slate900)
                .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = "HOW TO EXPORT GOOGLE DRIVE COOKIES:",
                    color = Cyan400,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "1. Open drive.google.com in Chrome or Firefox while signed in.\n" +
                            "2. Use an extension like 'Get cookies.txt LOCALLY' or copy DevTools network cookie header.\n" +
                    "3. Paste the contents above and tap 'SAVE COOKIES'.\n" +
                    "4. G-Drive DL will use this session to bypass quota errors and download private links seamlessly.",
                    color = Slate400,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
