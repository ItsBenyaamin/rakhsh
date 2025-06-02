package com.benyaamin.rakhsh.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.benyaamin.rakhsh.db.entity.DownloadEntity
import com.benyaamin.rakhsh.db.entity.DownloadMetadataEntity
import com.benyaamin.rakhsh.db.util.BitSetTypeConverter

@Database(
    version = 2,
    entities = [
        DownloadEntity::class,
        DownloadMetadataEntity::class,
    ],
    autoMigrations = [
        AutoMigration(1, 2)
    ]
)
@TypeConverters(BitSetTypeConverter::class)
abstract class RakhshDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
}