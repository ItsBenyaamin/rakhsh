package com.benyaamin.rakhsh.model

import java.util.BitSet

data class DownloadItem(
    val id: Int,
    val url: String,
    val path: String,
    val fileName: String,
    val tag: String?,
    val canResume: Boolean,
    val totalBytes: Long,
    val totalRead: Long,
    val ranges: BitSet?,
    val status: DownloadStatus,
    val group: String?,
    val headers: List<Pair<String, String>>,
    val error: ErrorType?,
) {

    override fun toString(): String {
        return "Id: $id, Url: $url, Path: $path, FileName:$fileName, Tag: $tag, TotalBytes: $totalBytes, totalRead: $totalRead, CanResume: $canResume, Status: ${status.name}, Error: ${error?.name}"
    }

}