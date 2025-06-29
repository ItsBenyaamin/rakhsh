package com.benyaamin.rakhsh.model

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

fun DownloadStatus.shouldStartAnother(): Boolean {
    return this == DownloadStatus.Completed || this == DownloadStatus.Error || this == DownloadStatus.Paused || this == DownloadStatus.Stopped
}