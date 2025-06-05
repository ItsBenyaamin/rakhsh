package com.benyaamin.rakhsh.client

import com.benyaamin.rakhsh.model.HeadResult
import java.io.InputStream
import java.net.URL

interface RakhshClient {
    fun headRequest(url: URL, headers: List<Pair<String, String>>): HeadResult
    fun createInputStream(url: URL, headers: List<Pair<String, String>>, range: LongRange?, readBlock: (InputStream) -> Unit, onError: (Exception) -> Unit)
}