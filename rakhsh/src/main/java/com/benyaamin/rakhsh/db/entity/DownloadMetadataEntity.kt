package com.benyaamin.rakhsh.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.BitSet

@Entity(
    foreignKeys = [
        ForeignKey(entity = DownloadEntity::class, parentColumns = ["id"], childColumns = ["itemId"])
    ]
)
data class DownloadMetadataEntity(
    @PrimaryKey
    val itemId: Int,
    val canResume: Boolean,
    val totalBytes: Long,
    val totalRead: Long,
    val ranges: BitSet?,
)
