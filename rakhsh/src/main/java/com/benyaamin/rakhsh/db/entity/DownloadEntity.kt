package com.benyaamin.rakhsh.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val url: String,
    val path: String,
    val fileName: String,
    val tag: String?,
    val status: String,
    val group: String?,
    val error: String?,
)