package com.benyaamin.rakhsh.model

import kotlinx.coroutines.flow.Flow

data class Download(
    val id: Int,
    val fileName: String,
    val status: DownloadStatus,
    val error: String?,
    val progressFlow: Flow<DownloadProgress>
)
