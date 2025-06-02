package com.benyaamin.rakhsh.core

import android.os.Handler
import com.benyaamin.rakhsh.client.RakhshClient
import com.benyaamin.rakhsh.model.ChunkDownloadResult
import com.benyaamin.rakhsh.model.DownloadItem
import com.benyaamin.rakhsh.model.DownloadStatus
import com.benyaamin.rakhsh.model.HeadResult
import com.benyaamin.rakhsh.util.Logger
import com.benyaamin.rakhsh.util.calculatePercentage
import com.benyaamin.rakhsh.util.mapToErrorType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.RandomAccessFile
import java.net.URL
import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class RakhshDownloader(
    private val mainHandler: Handler,
    private val scope: CoroutineScope,
    private val listener: DownloadStateInterface,
    private val logger: Logger,
    connectionCount: Int,
    private val chunkSize: Int,
    private val client: RakhshClient,
) {
    private val itemLock = Any()
    lateinit var item: DownloadItem private set
    private lateinit var url: URL
    private val pool = Executors.newFixedThreadPool(connectionCount)
    private val maxSingleThreadDownloadSize = 2 * 1024 * 1024
    private val totalRead = AtomicLong(0)
    private val lastPercentage = AtomicInteger(0)
    private var queue: ChunkQueue? = null

    @Volatile
    private var isStopped = false
    @Volatile
    private var isPaused = false

    fun setItem(item: DownloadItem) {
        this.item = item
        url = URL(item.url)
        var file = File(item.path)
        // if given path was directory, use url file name to create a file
        if (file.isDirectory) {
            file = File(file, item.fileName)
            this.item = item.copy(path = file.absolutePath)
            listener.onUpdateDownloadPath(item.id, file.absolutePath)
        }

        totalRead.set(item.totalRead)
    }

    fun prepare() {
        checkAcceptRanges(shouldStartDownload = false)
    }

    fun start() {
        val lastStatus = item.status
        updateItemStatus(DownloadStatus.Downloading)
        if (lastStatus == DownloadStatus.NotStarted) {
            logger.info("Download being started, sending head request...")
            checkAcceptRanges(shouldStartDownload = true)
        }
        else {
            resume()
        }
    }

    fun stop() {
        logger.debug { "setting isStopped to true" }
        isStopped = true
    }

    fun pause() {
        logger.debug { "setting isPaused to true" }
        isPaused = true
    }

    fun resume() {
        logger.debug { "resuming the download" }
        isPaused = false
        logger.debug { "update status to downloading" }
        updateItemStatus(DownloadStatus.Downloading)
        logger.debug { "init queue" }
        if (queue == null) queue = ChunkQueue(item.totalBytes, chunkSize, item.ranges)
        logger.debug { "calculate range" }
        queue?.calculateRanges()
        val percentage = calculatePercentage(item.totalRead, item.totalBytes)
        logger.debug { "Last progress: $percentage" }
        lastPercentage.set(percentage)
        logger.debug { "dispatch last progress" }
        listener.onProgressChanged(item.id, item.tag, item.totalBytes, item.totalRead, percentage)
        logger.debug { "start download" }
        startDownload(item.canResume, item.totalBytes)
    }

    private fun updateItemStatus(status: DownloadStatus, message: String? = null) {
        mainHandler.post {
            listener.onStatusChanged(item.id, status, message)
        }
        synchronized(itemLock) {
            item = item.copy(status = status)
        }
    }

    private fun checkAcceptRanges(shouldStartDownload: Boolean) {
        pool.execute {
            when(val result = client.headRequest(url)) {
                is HeadResult.Success -> {
                    item = item.copy(
                        totalBytes = result.totalBytes,
                        canResume = result.canResume
                    )

                    mainHandler.post {
                        listener.onUpdateInfo(
                            item.id,
                            totalBytes = result.totalBytes,
                            canResume = result.canResume
                        )
                    }

                    if (result.canResume) {
                        queue = ChunkQueue(result.totalBytes, chunkSize, item.ranges)
                        queue!!.calculateRanges()
                    }

                    if (shouldStartDownload) {
                        mainHandler.post {
                            startDownload(result.canResume, result.totalBytes)
                        }
                    }
                }

                is HeadResult.HttpError -> {
                    updateItemStatus(DownloadStatus.Error, result.statusCode.mapToErrorType())
                }

                is HeadResult.Failure -> {
                    updateItemStatus(DownloadStatus.Error, result.exception.mapToErrorType())
                }
            }
        }
    }

    private fun startDownload(canResume: Boolean, totalBytes: Long) {
        if (canResume && totalBytes > maxSingleThreadDownloadSize) downloadWithRange()
        else downloadSingleThread()
    }

    private fun downloadSingleThread() {
        pool.execute {
            var file = File(item.path)
            if (file.exists()) file.delete()

            val outputStream = file.outputStream()
            client.createInputStream(
                url,
                null,
                readBlock = { inputStream ->
                    val buffer = ByteArray(chunkSize)

                    while (!isPaused && !isStopped) {
                        val read = inputStream.read(buffer)
                        if (read == -1) break

                        outputStream.write(buffer, 0, read)
                        totalRead.addAndGet(read.toLong())
                        val percentage = calculatePercentage(totalRead.get(), item.totalBytes)

                        logger.debug {
                            "Download - id: ${item.id}, tag: ${item.tag}, totalBytes: ${item.totalBytes}, totalRead: $totalRead, percentage: $percentage"
                        }

                        if ((percentage - lastPercentage.get()) >= 2) {
                            lastPercentage.set(percentage)
                            mainHandler.post {
                                listener.onProgressChanged(item.id, item.tag, item.totalBytes, totalRead.get(), percentage)
                            }
                        }
                    }

                    if (isStopped) {
                        clearOnStop()
                        updateItemStatus(DownloadStatus.Stopped)
                        return@createInputStream
                    }

                    if (isPaused) {
                        updateItemStatus(DownloadStatus.Paused)
                        return@createInputStream
                    }
                    updateItemStatus(DownloadStatus.Completed)
                },
                onError = { msg ->
                    updateItemStatus(DownloadStatus.Error, msg.mapToErrorType())
                }
            )
        }
    }

    private fun downloadWithRange() {
        scope.launch {
            val tasks = mutableListOf<Callable<ChunkDownloadResult>>()
            while (true) {
                var range = queue!!.pull()
                if (range == null) {
                    val failedRange = queue!!.pullFromFailed()
                    if (failedRange == null) break

                    range = failedRange
                }

                val future = downloadChunkWithRange(range)
                tasks.add(future)
            }
            logger.debug {
                "Total chunks size: ${tasks.size}"
            }

            val results = mutableListOf<ChunkDownloadResult>()
            val futures = tasks.map { pool.submit(it) }
            for (future in futures) {
                try {
                    val result = future.get()
                    results.add(result)
                } catch (_: ExecutionException) {
                    // we only throw exception in the thread when stopped/paused
                    // so no need to do anything
                    break
                }
            }

            if (isStopped) {
                logger.info("Download Stopped.")
                clearOnStop()
                mainHandler.post {
                    updateItemStatus(DownloadStatus.Stopped)
                }
                return@launch
            }

            if (isPaused) {
                logger.info("Download paused.")
                mainHandler.post {
                    updateItemStatus(DownloadStatus.Paused)
                }
                return@launch
            }

            logger.debug {
                "Check for failed chunks..."
            }
            val failedChunks = results.filter { it.error != null }
            logger.info("Failed chunks count: ${failedChunks.size}")


            if (failedChunks.isNotEmpty()) {
                if (failedChunks.size == tasks.size) {
                    logger.debug {
                        "Download failed"
                    }
                    mainHandler.post {
                        updateItemStatus(DownloadStatus.Error, failedChunks.first().error?.mapToErrorType())
                    }
                } else {
                    logger.info("There is some failed chunks, downloading them again...")
                    mainHandler.post {
                        downloadWithRange()
                    }
                }
            }else {
                logger.info("Download Completed.")
            }
        }
    }

    private fun downloadChunkWithRange(range: IndexedValue<LongRange>) = object: Callable<ChunkDownloadResult> {
        override fun call(): ChunkDownloadResult? {
            // check for stop/pause on thread startup this needed because threads
            // are queued to run and could be stopped/paused before start
            if (isStopped || isPaused) throw CancellationException()

            var result = ChunkDownloadResult(range)

            client.createInputStream(
                url,
                range = range.value,
                readBlock = { inputStream ->
                    val buffer = ByteArray(chunkSize)
                    val raf = RandomAccessFile(item.path, "rw")
                    raf.seek(range.value.start)
                    var localTotalRead = 0L

                    while (!isPaused && !isStopped) {
                        val read = inputStream.read(buffer)
                        if (read == -1) break

                        raf.write(buffer, 0, read)
                        localTotalRead += read
                    }
                    raf.close()

                    if (isStopped) {
                        logger.debug { "Download is stopped, end the download chunk" }
                        result = result.copy(error = InterruptedException("stopped"))
                        return@createInputStream
                    }

                    if (isPaused) {
                        logger.debug { "Download is paused, end the download chunk" }
                        result = result.copy(error = InterruptedException("paused"))
                        return@createInputStream
                    }

                    val percentage = calculatePercentage(totalRead.get(), item.totalBytes)

                    logger.debug {
                        "id: ${item.id}, rangeIndex: ${range.index}, range: ${range.value.start}-${range.value.last}, percentage: $percentage"
                    }

                    if ((percentage - lastPercentage.get()) >= 2) {
                        lastPercentage.set(percentage)
                        mainHandler.post {
                            listener.onProgressChanged(item.id, item.tag, item.totalBytes, totalRead.get(), percentage)
                        }
                    }

                    totalRead.addAndGet(localTotalRead)
                    queue!!.setAsDone(range.index)
                    val lastState = queue!!.getLastState()
                    mainHandler.post {
                        listener.onUpdateRanges(item.id, lastState, totalRead.get())
                    }

                    synchronized(itemLock) {
                        item = item.copy(ranges = lastState)
                    }

                    // check if all ranges downloaded
                    // if true, dispatch the {DownloadStatus.Completed} state
                    if (queue!!.isCompleted()) {
                        mainHandler.post {
                            updateItemStatus(DownloadStatus.Completed)
                        }
                    }
                },
                onError = { err ->
                    queue!!.enqueueAsFailed(range)
                    result = result.copy(error = err)
                }
            )

            return result
        }

    }

    private fun clearOnStop() {
        pool.shutdownNow()
        queue = null
    }

    override fun equals(other: Any?): Boolean {
        if (other !is RakhshDownloader) return false
        return item.id == other.item.id
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}