package com.benyaamin.rakhsh

import android.content.Context
import com.benyaamin.rakhsh.client.HttpsUrlConnectionClient
import com.benyaamin.rakhsh.client.RakhshClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class Rakhsh {
    private var downloadManager: RakhshDownloadManager? = null
    private var scope: CoroutineScope = CoroutineScope(Job() + Dispatchers.IO)
    private var client: RakhshClient = HttpsUrlConnectionClient()
    private var mTag: String = "RakhshDownloadManager"
    private var connectionCount: Int = 6
    private var chunk: Int = 500 * 1024 // 500KB
    private var debug = false

    companion object {

        fun build(context: Context, block: Rakhsh.() -> Unit): RakhshDownloadManager {
            val rakhsh = Rakhsh()
            rakhsh.block()
            return rakhsh.initDownloadManager(context)
        }

    }

    /**
     * This library uses Coroutine to store values in database.
     * You can set a specific CoroutineScope if you want.
     */
    fun setCoroutineScope(scope: CoroutineScope) {
        this.scope = scope
    }

    /**
     * The default client is `HttpsUrlConnection`.
     * You can use other clients such as `OkHttpClient`.
     */
    fun setClient(client: RakhshClient) {
        this.client = client
    }

    fun setTag(tag: String) {
        mTag = tag
    }

    /**
     * Files with size more than `1MB` will downloaded with multiple thread.
     * every thread will download equal to chunk size.
     * the default value is `500 * 1024` which is `500KB`.
     */
    fun setDownloadChunk(chunk: Int) {
        this.chunk = chunk
    }

    fun debug() {
        this.debug = true
    }

    /**
     * default connection count for each multi-threaded download is `6`
     */
    fun setConnectionNum(num: Int) {
        connectionCount = num
    }

    private fun initDownloadManager(context: Context): RakhshDownloadManager {
        if (downloadManager == null) {
            downloadManager = RakhshDownloadManager(
                context,
                mTag,
                debug,
                scope,
                connectionCount,
                chunk,
                client,
            )
        }

        return downloadManager!!
    }

}