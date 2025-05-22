package com.benyaamin.rakhsh.util

import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

fun URL.getFilenameFromUrl(): String? {
    return try {
        val path = this.path

        if (path.isEmpty() || path == "/") {
            return null
        }
        val filenameWithPossibleSlash = if (path.startsWith("/")) {
            path.substring(1)
        } else {
            path
        }

        val decodedPath = URLDecoder.decode(filenameWithPossibleSlash, StandardCharsets.UTF_8.toString())

        val lastSlashIndex = decodedPath.lastIndexOf("/")
        if(lastSlashIndex == decodedPath.length - 1){
            return null
        }

        if (lastSlashIndex != -1) {
            decodedPath.substring(lastSlashIndex + 1)
        } else {
            decodedPath
        }
    } catch (e: Exception) {
        println("Error getting filename: ${e.message}")
        null
    }
}

fun calculatePercentage(totalRead: Long, totalBytes: Long): Int {
    if (totalBytes <= 0) {
        return 0
    }
    return ((totalRead.toDouble() / totalBytes.toDouble()) * 100.0).toInt()
}