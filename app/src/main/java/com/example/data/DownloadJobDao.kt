package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadJobDao {
    @Query("SELECT * FROM download_jobs ORDER BY id DESC")
    fun getAllJobs(): Flow<List<DownloadJobEntity>>

    @Query("SELECT * FROM download_jobs WHERE id = :id LIMIT 1")
    suspend fun getJobById(id: Long): DownloadJobEntity?

    @Query("SELECT * FROM download_jobs WHERE status = 'queued' ORDER BY id ASC LIMIT 1")
    suspend fun getNextQueuedJob(): DownloadJobEntity?

    @Query("SELECT * FROM download_jobs WHERE status = 'downloading' ORDER BY id ASC")
    suspend fun getActiveJobs(): List<DownloadJobEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: DownloadJobEntity): Long

    @Update
    suspend fun updateJob(job: DownloadJobEntity)

    @Query("DELETE FROM download_jobs WHERE id = :id")
    suspend fun deleteJob(id: Long)

    @Query("DELETE FROM download_jobs WHERE status = 'done'")
    suspend fun clearCompletedJobs()

    @Query("DELETE FROM download_jobs")
    suspend fun clearAllJobs()

    @Query("SELECT COUNT(*) FROM download_jobs")
    suspend fun getCount(): Int
}
