package com.benyaamin.rakhsh.okhttp

import com.benyaamin.rakhsh.client.RakhshClient
import com.benyaamin.rakhsh.model.HeadResult
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.net.URL

class OkHttpClient : RakhshClient {
    override fun headRequest(
        url: URL,
        headers: List<Pair<String, String>>
    ): HeadResult {
        try {
            val client = OkHttpClient()
            val builder = Request.Builder().url(url).head()

            headers.forEach {
                builder.addHeader(it.first, it.second)
            }

            val response = client.newCall(builder.build()).execute()

            val statusCode = response.code
            if (statusCode in 200..299) {
                val acceptRanges = response.header("Accept-Ranges")

                val doesAcceptRanges = when (acceptRanges?.lowercase()) {
                    "bytes" -> true
                    "none" -> false
                    else -> false
                }
                val length = response.header("Content-Length")?.toLong() ?: 0

                val success = HeadResult.Success(
                    length,
                    doesAcceptRanges
                )

                return success
            } else {
                return HeadResult.HttpError(statusCode)
            }
        }catch (e: Exception) {
            e.printStackTrace()
            return HeadResult.Failure(e)
        }
    }

    override fun createInputStream(
        url: URL,
        headers: List<Pair<String, String>>,
        range: LongRange?,
        readBlock: (InputStream) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val client = OkHttpClient()
            val builder = Request.Builder().url(url).get()

            headers.forEach {
                builder.addHeader(it.first, it.second)
            }

            range?.let {
                builder.addHeader("Range", "bytes=${range.start}-${range.last}")
            }

            val response = client.newCall(builder.build()).execute()
            val inputStream = response.body.byteStream()
            readBlock(inputStream)
            inputStream.close()
        }catch (e: Exception) {
            e.printStackTrace()
            onError(e)
        }
    }
}