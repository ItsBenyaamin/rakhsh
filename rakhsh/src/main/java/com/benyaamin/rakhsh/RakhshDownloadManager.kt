package com.benyaamin.rakhsh

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.room.Room
import com.benyaamin.rakhsh.client.RakhshClient
import com.benyaamin.rakhsh.core.DownloadStateInterface
import com.benyaamin.rakhsh.core.RakhshDownloader
import com.benyaamin.rakhsh.db.RakhshDatabase
import com.benyaamin.rakhsh.db.entity.DownloadMetadataEntity
import com.benyaamin.rakhsh.model.Download
import com.benyaamin.rakhsh.model.DownloadItem
import com.benyaamin.rakhsh.model.DownloadProgress
import com.benyaamin.rakhsh.model.DownloadRequest
import com.benyaamin.rakhsh.model.DownloadStatus
import com.benyaamin.rakhsh.model.ErrorType
import com.benyaamin.rakhsh.model.shouldRemoveFromOngoing
import com.benyaamin.rakhsh.util.Logger
import com.benyaamin.rakhsh.util.createDownloadHeader
import com.benyaamin.rakhsh.util.createDownloadItem
import com.benyaamin.rakhsh.util.getFilenameFromUrl
import com.benyaamin.rakhsh.util.toDownload
import com.benyaamin.rakhsh.util.toDownloadItem
import com.benyaamin.rakhsh.util.toEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.net.URL
import java.util.BitSet

class RakhshDownloadManager(
    private val context: Context,
    mTag: String,
    debug: Boolean,
    private val scope: CoroutineScope,
    private val connectionCount: Int,
    private val chunkSize: Int,
    private val client: RakhshClient,
) : DownloadStateInterface {
    private val mainHandler: Handler = Handler(Looper.getMainLooper())
    private val database = Room.databaseBuilder(
        context,
        RakhshDatabase::class.java,
        "rakhsh_download_manager"
    ).build()
    private val logger = Logger(debug, mTag)
    private val ongoingDownloadsList = arrayListOf<RakhshDownloader>()
    private val _progressFlow = MutableSharedFlow<DownloadProgress>(
        replay = 1,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val progressFlow = _progressFlow.asSharedFlow()

    init {
        logger.debug { "RakhshDownloadManager initialized" }
        scope.launch {
            // reset Paused and Downloading items to Stopped in start time
            database.downloadDao().resetStatusesTo(
                DownloadStatus.Paused.name,
                DownloadStatus.Stopped.name
            )
            database.downloadDao().resetStatusesTo(
                DownloadStatus.Downloading.name,
                DownloadStatus.Stopped.name
            )
        }
    }

    /**
     * returns a flow that contains all downloads that enqueued based on order.
     * it refreshes on each change on library's database.
     */
    fun getDownloadListFlow(asc: Boolean = true): Flow<List<Download>> {
        val flow = if (asc) {
            database.downloadDao().getListOfDownloadsAscFlow()
        } else {
            database.downloadDao().getListOfDownloadsDescFlow()
        }

        return flow.map { list ->
            list.map { entity ->
                val progressFlow = observeProgress(entity.id)
                entity.toDownload(progressFlow)
            }
        }.onEach {
            logger.debug {
                val type = if (asc) "(Asc)" else "(Desc)"
                "downloadList $type returned ${it.size} item"
            }
        }
    }

    /**
     * returns a flow that contains all downloads from a specific group that enqueued based on order.
     * it refreshes on each change on library's database.
     */
    fun getGroupDownloadListFlow(group: String, asc: Boolean = true): Flow<List<Download>> {
        val flow = if (asc) {
            database.downloadDao().getListOfGroupDownloadsAscFlow(group)
        } else {
            database.downloadDao().getListOfGroupDownloadsDescFlow(group)
        }

        return flow.map { list ->
            list.map { entity ->
                val progressFlow = observeProgress(entity.id)
                entity.toDownload(progressFlow)
            }
        }.onEach {
            logger.debug {
                val type = if (asc) "(Asc)" else "(Desc)"
                "groupDownloadList $type returned ${it.size} item"
            }
        }
    }

    /**
     * returns a flow for a specific download item based on id returned at enqueue
     */
    fun observeProgress(id: Int) = _progressFlow.filter { it.id == id }

    /**
     * returns a flow for a specific download item based on tag set at enqueue
     */
    fun observeProgress(tag: String) = _progressFlow.filter { it.tag == tag }

    /**
     * returns a flow for a specific group of items
     */
    fun observeGroupProgress(group: String) = _progressFlow.filter { it.group == group }

    /**
     * create download request with given info.
     * You can pass a folder as path, in this case, the fileName from url will used. if you don't pass path, Context.filesDir will use as folder.
     * @param url Link of file you want to download.
     * @param path Destination to create file
     * @param tag A string identifier to this download request
     * @return created download item id
     */
    suspend fun enqueue(
        url: String,
        path: String? = null,
        tag: String? = null,
        group: String? = null
    ): Int {
        var tempPath = ""
        if (path != null) {
            tempPath = path
        } else {
            tempPath = context.filesDir.absolutePath
        }

        val fileName = URL(url).getFilenameFromUrl() ?: ""
        val item = createDownloadItem(url, tempPath, fileName, tag, group).toEntity()
        val id = database.downloadDao().insertRequest(item)

        val metadata = DownloadMetadataEntity(id.toInt(), false, 0, 0, null)
        database.downloadDao().insertMetadata(metadata)

        logger.debug {
            "enqueue new download request -> Url: $url, Path: $path, FileName: $fileName, Tag: $tag, Group: $group"
        }
        return id.toInt()
    }

    /**
     * create download request in more advanced way.
     * @param request Request information
     * @return created download item id
     */
    suspend fun enqueue(request: DownloadRequest): Int {
        val id = enqueue(request.url, request.path, request.tag, request.group)

        val headers = request.headers.map { createDownloadHeader(id, it.first, it.second) }
        database.downloadDao().insertHeaders(headers)

        return id
    }

    suspend fun prepare(id: Int) {
        val item = database.downloadDao().getDownloadById(id)!!.toDownloadItem()
        prepare(item)
    }

    suspend fun prepare(tag: String) {
        val item = database.downloadDao().getDownloadByTag(tag)!!.toDownloadItem()
        prepare(item)
    }

    private fun prepare(item: DownloadItem) {
        val downloader = RakhshDownloader(
            mainHandler,
            scope,
            this,
            logger,
            connectionCount,
            chunkSize,
            client,
        )
        downloader.setItem(item)
        ongoingDownloadsList.add(downloader)

        downloader.prepare()
    }

    /**
     * If status of item were NotStarted, then start the download.
     * otherwise, it will resume the download.
     */
    suspend fun start(tag: String) {
        val ongoingItem = ongoingDownloadsList.find { it.item.tag == tag }
        if (ongoingItem != null) {
            ongoingItem.resume()
            return
        }

        database.downloadDao().getDownloadByTag(tag)?.toDownloadItem()?.let {
            mainHandler.post {
                startDownload(it)
            }
        }
    }

    /**
     * If status of item were NotStarted, then start the download.
     * otherwise, it will resume the download.
     */
    suspend fun start(id: Int) {
        database.downloadDao().getDownloadById(id)?.toDownloadItem()?.let {
            mainHandler.post {
                startDownload(it)
            }
        }
    }

    private fun startDownload(item: DownloadItem) {
        val exists = ongoingDownloadsList.find { it.item.id == item.id }

        if (exists != null) {
            exists.start()
        } else {

            val downloader = RakhshDownloader(
                mainHandler,
                scope,
                this,
                logger,
                connectionCount,
                chunkSize,
                client,
            )
            downloader.setItem(item)
            ongoingDownloadsList.add(downloader)
            downloader.start()
        }

        logger.debug {
            "start downloading item: $item"
        }
    }

    /**
     * stop the download and remove all the resources from memory
     */
    fun stop(id: Int) {
        ongoingDownloadsList.find { it.item.id == id }?.let { downloader ->
            logger.debug {
                "stopping item: ${downloader.item}"
            }

            downloader.stop()
            ongoingDownloadsList.remove(downloader)
        }
    }

    /**
     * stop the download and remove all the resources from memory
     */
    fun stop(tag: String) {
        ongoingDownloadsList.find { it.item.tag == tag }?.let { downloader ->
            logger.debug {
                "stopping item: ${downloader.item}"
            }

            downloader.stop()
            ongoingDownloadsList.remove(downloader)
        }
    }

    /**
     * pause the download, with ready to resume resource
     */
    fun pause(id: Int) {
        ongoingDownloadsList.find { it.item.id == id }?.let { downloader ->
            logger.debug {
                "pausing item: ${downloader.item}"
            }

            downloader.pause()
        }
    }

    /**
     * pause the download, with ready to resume resource
     */
    fun pause(tag: String) {
        ongoingDownloadsList.find { it.item.tag == tag }?.let { downloader ->
            logger.debug {
                "pausing item: ${downloader.item}"
            }

            downloader.pause()
        }
    }

    /**
     * If already is in memory, It will resume the download.
     * otherwise, it will start the download and then resume it.
     */
    fun resume(id: Int) {
        val downloader = ongoingDownloadsList.find { it.item.id == id }
        if (downloader != null) {
            downloader.resume()

            logger.debug {
                "resume item: ${downloader.item}"
            }
        } else {
            scope.launch { start(id) }
        }
    }

    /**
     * If already is in memory, It will resume the download.
     * otherwise, it will start the download and then resume it.
     */
    fun resume(tag: String) {
        val downloader = ongoingDownloadsList.find { it.item.tag == tag }
        if (downloader != null) {
            downloader.resume()

            logger.debug {
                "resume item: ${downloader.item}"
            }
        } else {
            scope.launch { start(tag) }
        }
    }

    /**
     * Stops the ongoing download if any, and removes the download item from database
     */
    suspend fun remove(id: Int) {
        ongoingDownloadsList.find { it.item.id == id }?.let {
            it.stop()
            ongoingDownloadsList.remove(it)
        }

        database.downloadDao().deleteRequestMetadata(id)
        database.downloadDao().deleteRequest(id)
    }

    /**
     * Stops the ongoing download if any, and removes the download item from database
     */
    suspend fun remove(tag: String) {
        ongoingDownloadsList.find { it.item.tag == tag }?.let {
            it.stop()
            ongoingDownloadsList.remove(it)
        }

        val item = database.downloadDao().getDownloadByTag(tag)
        if (item != null) {
            database.downloadDao().deleteRequestMetadata(item.downloadEntity.id)
            database.downloadDao().deleteRequest(item.downloadEntity.id)
        }
    }


    /**
     * downloading item events
     */
    override fun onUpdateDownloadPath(downloadId: Int, newPath: String) {
        scope.launch {
            database.downloadDao().updateDownloadPath(downloadId, newPath)
        }
    }

    override fun onUpdateInfo(
        downloadId: Int,
        totalBytes: Long,
        canResume: Boolean
    ) {
        logger.debug {
            "Download - id: $downloadId, totalBytes: $totalBytes, canResume: $canResume"
        }
        scope.launch {
            database.downloadDao().updateRequestInfo(
                downloadId = downloadId,
                totalBytes = totalBytes,
                canResume = canResume
            )
        }
    }

    override fun onStatusChanged(downloadId: Int, stataus: DownloadStatus, error: ErrorType?) {
        if (stataus == DownloadStatus.Error) {
            logger.error("Download - id: $downloadId, status: ${stataus.name}, error: ${error?.name}, cause: ${error?.error}")
        } else {
            logger.info("Download - id: $downloadId, status: ${stataus.name}")
        }

        if (stataus.shouldRemoveFromOngoing()) {
            ongoingDownloadsList.removeIf { it.item.id == downloadId }
        }

        scope.launch {
            database.downloadDao().updateDownloadState(
                downloadId = downloadId,
                stataus.name,
                error?.name,
            )
        }
    }

    override fun onUpdateRanges(downloadId: Int, bitset: BitSet, totalRead: Long) {
        scope.launch {
            database.downloadDao().updateDownloadRangesAndRead(downloadId, bitset, totalRead)
        }
    }

    override fun onProgressChanged(
        downloadId: Int,
        tag: String?,
        group: String?,
        totalBytes: Long,
        totalRead: Long,
        progress: Int
    ) {
        val info = DownloadProgress(downloadId, tag, group, totalRead, totalRead, progress)
        logger.info(info.toString())
        _progressFlow.tryEmit(info)
    }

}