package com.benyaamin.rakhsh.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.benyaamin.rakhsh.db.entity.DownloadEntity
import com.benyaamin.rakhsh.db.entity.DownloadHeaders
import com.benyaamin.rakhsh.db.entity.DownloadMetadataEntity
import com.benyaamin.rakhsh.db.projection.SimpleDownloadEntity
import kotlinx.coroutines.flow.Flow
import java.util.BitSet

@Dao
interface DownloadDao {
    @Query("select id, fileName, status, error from downloadentity order by id asc")
    fun getListOfDownloadsAscFlow(): Flow<List<SimpleDownloadEntity>>

    @Query("select id, fileName, status, error from downloadentity order by id desc")
    fun getListOfDownloadsDescFlow(): Flow<List<SimpleDownloadEntity>>

    @Query("select id, fileName, status, `group`, error from downloadentity where `group` = :group order by id asc")
    fun getListOfGroupDownloadsAscFlow(group: String): Flow<List<SimpleDownloadEntity>>

    @Query("select id, fileName, status, `group`, error from downloadentity where `group` = :group order by id desc")
    fun getListOfGroupDownloadsDescFlow(group: String): Flow<List<SimpleDownloadEntity>>

    @Query("select * from downloadentity where tag = :tag")
    suspend fun getDownloadByTag(tag: String): FullDownloadItem?

    @Query("select * from downloadentity where id = :id")
    suspend fun getDownloadById(id: Int): FullDownloadItem?

    @Insert
    suspend fun insertRequest(request: DownloadEntity): Long

    @Query("update downloadentity set status = :status, error = :error where id = :downloadId")
    suspend fun updateDownloadState(downloadId: Int, status: String, error: String?)

    @Query("update downloadentity set path = :path where id = :downloadId")
    fun updateDownloadPath(downloadId: Int, path: String)

    @Query("update downloadentity set status = :newStatus where status = :oldStatus")
    suspend fun resetStatusesTo(oldStatus: String, newStatus: String)

    @Query("delete from downloadentity where id = :id")
    suspend fun deleteRequest(id: Int)


    /**
     * Metadata
     */
    @Insert
    suspend fun insertMetadata(metadata: DownloadMetadataEntity)

    @Query("update downloadmetadataentity set totalBytes = :totalBytes, canResume = :canResume where itemId = :downloadId")
    suspend fun updateRequestInfo(downloadId: Int, totalBytes: Long, canResume: Boolean)

    @Query("update downloadmetadataentity set ranges = :set, totalRead = :totalRead where itemId = :downloadId")
    suspend fun updateDownloadRangesAndRead(downloadId: Int, set: BitSet, totalRead: Long)

    @Query("delete from downloadmetadataentity where itemId = :id")
    suspend fun deleteRequestMetadata(id: Int)

    /**
     * Headers
     */
    @Insert
    suspend fun insertHeaders(list: List<DownloadHeaders>)

    @Query("delete from downloadheaders where itemId = :id")
    suspend fun deleteDownloadHeaders(id: Int)
}