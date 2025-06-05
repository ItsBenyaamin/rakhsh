package com.benyaamin.rakhsh.util

import com.benyaamin.rakhsh.model.DownloadStatus
import com.benyaamin.rakhsh.db.FullDownloadItem
import com.benyaamin.rakhsh.model.DownloadItem
import com.benyaamin.rakhsh.db.entity.DownloadEntity
import com.benyaamin.rakhsh.db.projection.SimpleDownloadEntity
import com.benyaamin.rakhsh.model.Download
import com.benyaamin.rakhsh.model.DownloadProgress
import com.benyaamin.rakhsh.model.ErrorType
import kotlinx.coroutines.flow.Flow

fun FullDownloadItem.toDownloadItem(): DownloadItem {
    val error = if (downloadEntity.error == null) null
    else ErrorType.valueOf(downloadEntity.error)
    val mappedHeaders = headers.map { Pair(it.key, it.value) }
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
        group = downloadEntity.group,
        headers = mappedHeaders,
        error = error
    )
}

fun SimpleDownloadEntity.toDownload(progressFlow: Flow<DownloadProgress>): Download {
    val error = if (error == null) null
    else ErrorType.valueOf(error)
    return Download(
        id = id,
        fileName = fileName,
        status = DownloadStatus.valueOf(status),
        group = group,
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
        group = group,
        error = null,
    )
}