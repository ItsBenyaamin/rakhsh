package com.benyaamin.rakhsh.util

import com.benyaamin.rakhsh.DownloadStatus
import com.benyaamin.rakhsh.model.DownloadItem

fun createDownloadItem(url: String, path: String, fileName: String, tag: String?): DownloadItem {
    return DownloadItem(
        id = 0,
        url = url,
        path = path,
        fileName = fileName,
        tag = tag ?: "",
        canResume = false,
        totalBytes = 0,
        totalRead = 0,
        ranges = null,
        status = DownloadStatus.NotStarted,
        error = "",
    )
}