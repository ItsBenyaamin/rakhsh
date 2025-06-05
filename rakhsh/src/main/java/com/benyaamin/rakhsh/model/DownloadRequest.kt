package com.benyaamin.rakhsh.model

data class DownloadRequest(
    val url: String,
    val path: String?,
    val tag: String?,
    val group: String?,
    val headers: List<Pair<String, String>>,
)
