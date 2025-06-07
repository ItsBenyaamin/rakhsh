package com.benyaamin.rakhsh.model

sealed class DownloadState {
    data class Downloading(val downloadId: Int, val tag: String?) : DownloadState()
    data class Stopped(val downloadId: Int, val tag: String?) : DownloadState()
    data class Paused(val downloadId: Int, val tag: String?) : DownloadState()
    data class Completed(val downloadId: Int, val tag: String?) : DownloadState()
    data class Error(val downloadId: Int, val tag: String?, val errType: ErrorType) : DownloadState()
}