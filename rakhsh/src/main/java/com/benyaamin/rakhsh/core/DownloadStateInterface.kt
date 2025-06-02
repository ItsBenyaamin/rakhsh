package com.benyaamin.rakhsh.core

import com.benyaamin.rakhsh.model.DownloadStatus
import com.benyaamin.rakhsh.model.ErrorType
import java.util.BitSet

interface DownloadStateInterface {
    fun onUpdateDownloadPath(downloadId: Int, newPath: String)
    fun onUpdateInfo(downloadId: Int, totalBytes: Long, canResume: Boolean)
    fun onStatusChanged(downloadId: Int, status: DownloadStatus, error: ErrorType?)
    fun onUpdateRanges(downloadId: Int, bitset: BitSet, totalRead: Long)
    fun onProgressChanged(downloadId: Int, tag: String?, totalBytes: Long, totalRead: Long, progress: Int)
}