package com.benyaamin.rakhsh.client

import com.benyaamin.rakhsh.model.HeadResult
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class HttpsUrlConnectionClient : RakhshClient {
    override fun headRequest(url: URL): HeadResult {
        try {
            val connection = url.openConnection() as HttpsURLConnection
            connection.requestMethod = "HEAD"
            connection.connect()

            val statusCode = connection.responseCode
            if (statusCode in 200..299) {
                val acceptRanges = connection.getHeaderField("Accept-Ranges")
                println("Accept-Ranges: $acceptRanges")

                val doesAcceptRanges = when (acceptRanges?.lowercase()) {
                    "bytes" -> true
                    "none" -> false
                    else -> false
                }

                val length = connection.contentLengthLong

                val success = HeadResult.Success(
                    length,
                    doesAcceptRanges
                )
                connection.disconnect()
                return success
            } else {
                connection.disconnect()
                return HeadResult.HttpError(statusCode)
            }
        }catch (e: Exception) {
            e.printStackTrace()
            return HeadResult.Failure(e)
        }
    }

    override fun createInputStream(url: URL, range: LongRange?, readBlock: (InputStream) -> Unit, onError: (Exception) -> Unit) {
        try {
            val connection = url.openConnection() as HttpsURLConnection
            connection.requestMethod = "GET"
            range?.let {
                connection.setRequestProperty("Range", "bytes=${range.start}-${range.last}")
            }
            connection.connect()
            val inputStream = connection.getInputStream()
            readBlock(inputStream)
            inputStream.close()
            connection.disconnect()
        }catch (e: Exception) {
            e.printStackTrace()
            onError(e)
        }
    }
}