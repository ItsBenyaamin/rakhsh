package com.benyaamin.rakhsh.db

import androidx.room.Embedded
import androidx.room.Relation
import com.benyaamin.rakhsh.db.entity.DownloadEntity
import com.benyaamin.rakhsh.db.entity.DownloadMetadataEntity

data class FullDownloadItem(
    @Embedded val downloadEntity: DownloadEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "itemId"
    )
    val metadata: DownloadMetadataEntity
)
