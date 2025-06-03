package com.benyaamin.rakhsh.model

import kotlinx.coroutines.flow.Flow

data class Download(
    val id: Int,
    val fileName: String,
    val status: DownloadStatus,
    val group: String?,
    val error: ErrorType?,
    val progressFlow: Flow<DownloadProgress>
)
