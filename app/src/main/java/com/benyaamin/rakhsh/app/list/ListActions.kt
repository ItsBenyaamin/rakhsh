package com.benyaamin.rakhsh.app.list

import com.benyaamin.rakhsh.model.DownloadStatus

sealed class ListActions {
    data class EnqueueNewDownload(val url: String) : ListActions()
    data class StartDownload(val id: Int, val status: DownloadStatus) : ListActions()
    data class StopDownload(val id: Int) : ListActions()
    data class PauseDownload(val id: Int) : ListActions()
    data class RemoveDownload(val id: Int) : ListActions()
}