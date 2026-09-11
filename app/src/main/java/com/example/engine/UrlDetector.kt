package com.example.engine

import java.net.URI

object UrlDetector {

    data class DetectionResult(
        val backend: String,
        val suggestedTitle: String,
        val format: String,
        val isDirectResolvable: Boolean = false,
        val directDownloadUrl: String? = null
    )

    fun detect(url: String, explicitDirect: Boolean = false, explicitGallery: Boolean = false): DetectionResult {
        val trimmed = url.trim()
        val lower = trimmed.lowercase()

        // 1. Torrent / Magnet
        if (lower.startsWith("magnet:") || lower.split("?").firstOrNull()?.endsWith(".torrent") == true) {
            val title = extractMagnetDisplayName(trimmed) ?: "Torrent Download"
            return DetectionResult(
                backend = "aria2",
                suggestedTitle = title,
                format = "Torrent/Magnet"
            )
        }

        // 2. Gallery / Image Art
        if (explicitGallery || lower.contains("instagram.com") || lower.contains("deviantart.com") || lower.contains("artstation.com")) {
            val name = extractBasename(trimmed) ?: "Gallery Media Asset"
            return DetectionResult(
                backend = "gallery",
                suggestedTitle = name,
                format = "Gallery"
            )
        }

        // 3. Google Drive / Colab
        if (lower.contains("drive.google.com") || lower.contains("docs.google.com") || lower.contains("colab.research.google.com")) {
            val fileId = extractGoogleDriveId(trimmed)
            val colabId = extractColabId(trimmed)
            val title = when {
                colabId != null -> "Colab Notebook [$colabId]"
                fileId != null -> "Google Drive File [$fileId]"
                else -> "Google Drive Shared File"
            }
            val directUrl = fileId?.let { "https://drive.google.com/uc?export=download&id=$it&confirm=t" }
            return DetectionResult(
                backend = "gdrive",
                suggestedTitle = title,
                format = "Google Drive",
                isDirectResolvable = directUrl != null,
                directDownloadUrl = directUrl
            )
        }

        // 4. Direct HTTP / Archive / Document
        val isDirectExtension = Regex("""\.(zip|tar|gz|iso|bin|exe|pdf|dmg|pkg|7z|apk|mp4|mkv|mov|avi|mp3|flac|wav)(\?.*)?$""", RegexOption.IGNORE_CASE)
            .containsMatchIn(lower)

        if (explicitDirect || isDirectExtension) {
            val title = extractBasename(trimmed) ?: "HTTP Direct File"
            return DetectionResult(
                backend = "direct",
                suggestedTitle = title,
                format = "HTTP Direct",
                isDirectResolvable = true,
                directDownloadUrl = trimmed
            )
        }

        // 5. YouTube / Media Stream
        if (lower.contains("youtube.com") || lower.contains("youtu.be") || lower.contains("vimeo.com") || lower.contains("twitch.tv") || lower.contains("tiktok.com")) {
            val videoId = extractYouTubeId(trimmed)
            val title = if (videoId != null) "YouTube Media [$videoId]" else "Media Stream"
            return DetectionResult(
                backend = "yt-dlp",
                suggestedTitle = title,
                format = "1080p MP4"
            )
        }

        // Fallback: Direct or Media
        val fallbackBasename = extractBasename(trimmed)
        return DetectionResult(
            backend = "direct",
            suggestedTitle = fallbackBasename ?: "Remote Download Asset",
            format = "AUTO",
            isDirectResolvable = true,
            directDownloadUrl = trimmed
        )
    }

    fun extractGoogleDriveId(url: String): String? {
        // Pattern 1: /file/d/ID/...
        val p1 = Regex("""/file/d/([a-zA-Z0-9_-]+)""").find(url)
        if (p1 != null) return p1.groupValues[1]

        // Pattern 2: id=ID
        val p2 = Regex("""[?&]id=([a-zA-Z0-9_-]+)""").find(url)
        if (p2 != null) return p2.groupValues[1]

        // Pattern 3: /d/ID
        val p3 = Regex("""/d/([a-zA-Z0-9_-]+)""").find(url)
        if (p3 != null) return p3.groupValues[1]

        return null
    }

    fun extractColabId(url: String): String? {
        val match = Regex("""/drive/([a-zA-Z0-9_-]+)""").find(url)
        return match?.groupValues?.get(1)
    }

    fun extractYouTubeId(url: String): String? {
        val p1 = Regex("""(?:v=|youtu\.be/|embed/)([a-zA-Z0-9_-]{11})""").find(url)
        return p1?.groupValues?.get(1)
    }

    fun extractMagnetDisplayName(url: String): String? {
        val match = Regex("""[?&]dn=([^&]+)""").find(url)
        return match?.groupValues?.get(1)?.let {
            try {
                java.net.URLDecoder.decode(it, "UTF-8")
            } catch (e: Exception) {
                it
            }
        }
    }

    private fun extractBasename(url: String): String? {
        return try {
            val uri = URI(url)
            val path = uri.path ?: ""
            val lastSegment = path.substringAfterLast('/')
            if (lastSegment.isNotBlank() && lastSegment.length > 2) {
                java.net.URLDecoder.decode(lastSegment, "UTF-8")
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
