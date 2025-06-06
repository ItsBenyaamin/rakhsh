package com.benyaamin.rakhsh.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.benyaamin.rakhsh.db.entity.DownloadEntity
import com.benyaamin.rakhsh.db.entity.DownloadHeaders
import com.benyaamin.rakhsh.db.entity.DownloadMetadataEntity
import com.benyaamin.rakhsh.db.util.BitSetTypeConverter

@Database(
    version = 5,
    entities = [
        DownloadEntity::class,
        DownloadMetadataEntity::class,
        DownloadHeaders::class,
    ],
    autoMigrations = [
        AutoMigration(1, 2),
        AutoMigration(2, 3),
        AutoMigration(3, 4),
        AutoMigration(4, 5),
    ]
)
@TypeConverters(BitSetTypeConverter::class)
abstract class RakhshDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
}