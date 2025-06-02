package com.benyaamin.rakhsh.model

sealed class HeadResult {
    data class Success(val totalBytes: Long, val canResume: Boolean) : HeadResult()
    data class HttpError(val statusCode: Int) : HeadResult()
    data class Failure(val exception: Exception) : HeadResult()
}