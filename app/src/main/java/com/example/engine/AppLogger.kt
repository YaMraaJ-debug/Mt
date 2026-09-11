package com.example.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogLevel {
    INFO, WARN, ERROR, SUCCESS
}

data class LogEntry(
    val timestamp: String,
    val message: String,
    val level: LogLevel
)

object AppLogger {
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun log(message: String, level: LogLevel = LogLevel.INFO) {
        val entry = LogEntry(
            timestamp = dateFormat.format(Date()),
            message = message,
            level = level
        )
        val current = _logs.value
        // Retain last 300 logs
        val updated = if (current.size >= 300) {
            current.takeLast(250) + entry
        } else {
            current + entry
        }
        _logs.value = updated
    }

    fun clear() {
        _logs.value = emptyList()
        log("Logs buffer cleared", LogLevel.INFO)
    }

    init {
        log("G-Drive DL Engine initialized [Version 29.0]", LogLevel.SUCCESS)
        log("Backend handlers registered: yt-dlp, gdrive, direct, aria2, gallery", LogLevel.INFO)
    }
}
