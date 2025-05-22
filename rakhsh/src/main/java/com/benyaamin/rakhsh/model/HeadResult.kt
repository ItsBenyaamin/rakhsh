package com.benyaamin.rakhsh.model

data class HeadResult(
    val success: HeadResultSuccess?,
    val error: String?,
)

data class HeadResultSuccess(
    val totalBytes: Long,
    val canResume: Boolean,
)