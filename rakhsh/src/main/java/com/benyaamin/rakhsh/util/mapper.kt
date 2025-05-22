package com.benyaamin.rakhsh.util

import com.benyaamin.rakhsh.DownloadStatus
import com.benyaamin.rakhsh.db.FullDownloadItem
import com.benyaamin.rakhsh.model.DownloadItem
import com.benyaamin.rakhsh.db.entity.DownloadEntity
import com.benyaamin.rakhsh.db.projection.SimpleDownloadEntity
import com.benyaamin.rakhsh.model.Download
import com.benyaamin.rakhsh.model.DownloadProgress
import kotlinx.coroutines.flow.Flow

fun FullDownloadItem.toDownloadItem(): DownloadItem {
    return DownloadItem(
        id = downloadEntity.id,
        url = downloadEntity.url,
        path = downloadEntity.path,
        fileName = downloadEntity.fileName,
        tag = downloadEntity.tag,
        canResume = metadata.canResume,
        totalBytes = metadata.totalBytes,
        totalRead = metadata.totalRead,
        ranges = metadata.ranges,
        status = DownloadStatus.valueOf(downloadEntity.status),
        error = downloadEntity.error
    )
}

fun SimpleDownloadEntity.toDownload(progressFlow: Flow<DownloadProgress>): Download {
    return Download(
        id = id,
        fileName = fileName,
        status = DownloadStatus.valueOf(status),
        error = error,
        progressFlow = progressFlow
    )
}

fun DownloadItem.toEntity(): DownloadEntity {
    return DownloadEntity(
        id = 0,
        url = url,
        path = path,
        fileName = fileName,
        tag = tag,
        status = DownloadStatus.NotStarted.name,
        error = null
    )
}