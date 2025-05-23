package com.benyaamin.rakhsh

import java.util.BitSet

interface DownloadStateInterface {
    fun onUpdateDownloadPath(downloadId: Int, newPath: String)
    fun onUpdateInfo(downloadId: Int, totalBytes: Long, canResume: Boolean)
    fun onStatusChanged(downloadId: Int, status: DownloadStatus, message: String?)
    fun onUpdateRanges(downloadId: Int, bitset: BitSet, totalRead: Long)
    fun onProgressChanged(downloadId: Int, tag: String?, totalBytes: Long, totalRead: Long, progress: Int)
}