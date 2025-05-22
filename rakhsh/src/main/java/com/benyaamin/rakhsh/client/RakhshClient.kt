package com.benyaamin.rakhsh.client

import com.benyaamin.rakhsh.model.HeadResult
import java.io.InputStream
import java.net.URL

interface RakhshClient {
    fun headRequest(url: URL): HeadResult
    fun createInputStream(url: URL, range: LongRange?, readBlock: (InputStream) -> Unit, onError: (Throwable) -> Unit)
}