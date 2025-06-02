package com.benyaamin.rakhsh.model

data class ChunkDownloadResult(
    val range: IndexedValue<LongRange>,
    val isSuccess: Boolean = false,
    val error: Exception? = null,
)
