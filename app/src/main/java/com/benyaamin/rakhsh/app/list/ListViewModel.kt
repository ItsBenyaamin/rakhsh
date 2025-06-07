package com.benyaamin.rakhsh.app.list

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaamin.rakhsh.model.DownloadStatus
import com.benyaamin.rakhsh.Rakhsh
import com.benyaamin.rakhsh.RakhshDownloadManager
import com.benyaamin.rakhsh.model.Download
import com.benyaamin.rakhsh.model.DownloadState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ListViewModel : ViewModel() {
    private lateinit var downloadManager: RakhshDownloadManager
    lateinit var downloadsList: Flow<List<Download>>


    fun init(context: Context) {
        downloadManager = Rakhsh.build(context) {
            debug()
            setTag("RDM")
            setConnectionNum(4)
            setDownloadChunk(1024 * 1024)
        }

        downloadsList = downloadManager.getDownloadListFlow(asc = false)


        viewModelScope.launch {
            downloadManager.downloadsStateFlow.collect { state ->
                when(state) {
                    is DownloadState.Downloading -> {
                        Log.d("RakhshApp", "Download with Id: ${state.downloadId} is started downloading")
                    }
                    is DownloadState.Completed -> {
                        Log.d("RakhshApp", "Download with Id: ${state.downloadId} is completed")
                    }

                    is DownloadState.Error -> {
                        Log.d("RakhshApp", "Download with Id: ${state.downloadId} faced an error." +
                                " ErrorType: ${state.errType.name}, cause: ${state.errType.error}")
                    }

                    is DownloadState.Paused -> {
                        Log.d("RakhshApp", "Download with Id: ${state.downloadId} is paused")
                    }

                    is DownloadState.Stopped -> {
                        Log.d("RakhshApp", "Download with Id: ${state.downloadId} is stopped")
                    }
                }
            }
        }
    }

    fun processAction(action: ListActions) {
        when(action) {
            is ListActions.EnqueueNewDownload -> {
                viewModelScope.launch {
                    val id = downloadManager.enqueue(action.url)
                    downloadManager.prepare(id)
                }
            }

            is ListActions.PauseDownload -> {
                downloadManager.pause(action.id)
            }

            is ListActions.RemoveDownload -> {
                viewModelScope.launch {
                    downloadManager.remove(action.id)
                }
            }

            is ListActions.StartDownload -> {
                if (action.status == DownloadStatus.Paused) {
                    downloadManager.resume(action.id)
                } else {
                    viewModelScope.launch {
                        downloadManager.start(action.id)
                    }
                }
            }

            is ListActions.StopDownload -> {
                downloadManager.stop(action.id)
            }
        }
    }

}