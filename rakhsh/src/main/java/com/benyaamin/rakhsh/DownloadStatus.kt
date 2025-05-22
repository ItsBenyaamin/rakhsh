package com.benyaamin.rakhsh

enum class DownloadStatus {
    NotStarted,
    Downloading,
    Paused,
    Stopped,
    Completed,
    Error
}

fun DownloadStatus.shouldRemoveFromOngoing(): Boolean {
    return this == DownloadStatus.Completed || this == DownloadStatus.Error || this == DownloadStatus.Stopped
}