package com.benyaamin.rakhsh.util

import com.benyaamin.rakhsh.db.entity.DownloadHeaders

fun createDownloadHeader(itemId: Int, key: String, value: String): DownloadHeaders {
    return DownloadHeaders(
        0,
        itemId,
        key,
        value,
    )
}