package com.benyaamin.rakhsh.db.projection

data class SimpleDownloadEntity(
    val id: Int,
    val fileName: String,
    val status: String,
    val group: String?,
    val error: String?,
)