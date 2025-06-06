package com.benyaamin.rakhsh.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(entity = DownloadEntity::class, parentColumns = ["id"], childColumns = ["itemId"])
    ],
    indices = [
        Index(value = ["itemId"])
    ]
)
data class DownloadHeaders(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val itemId: Int,
    val key: String,
    val value: String,
)
