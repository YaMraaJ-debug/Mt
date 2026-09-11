package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "download_jobs")
data class DownloadJobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val url: String,
    val backend: String, // "gdrive", "yt-dlp", "direct", "aria2", "gallery"
    val status: String,  // "queued", "downloading", "done", "failed", "paused"
    val title: String,
    val error: String? = null,
    val created: Long = System.currentTimeMillis(),
    val started: Long? = null,
    val finished: Long? = null,
    val attempts: Int = 0,
    val bytes: Long = 0L,
    val total: Long = 0L,
    val speed: Long = 0L, // bytes per second
    val output: String = "~/storage/downloads",
    val fmt: String = "AUTO"
)
