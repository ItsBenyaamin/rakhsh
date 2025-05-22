package com.benyaamin.rakhsh.model

data class DownloadProgress(
    val id: Int,
    val tag: String?,
    val totalBytes: Long,
    val totalRead: Long,
    val progress: Int,
) {

    override fun toString(): String {
        return "Download - id: $id, tag: $tag, totalBytes: $totalBytes, totalRead: $totalRead, progress: $progress"
    }

}
