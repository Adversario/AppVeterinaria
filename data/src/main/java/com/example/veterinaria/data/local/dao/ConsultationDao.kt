package com.example.veterinaria.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.veterinaria.data.local.entity.ConsultationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConsultationDao {
    @Query("SELECT * FROM consultations ORDER BY id")
    fun observeAll(): Flow<List<ConsultationEntity>>

    @Query("SELECT * FROM consultations ORDER BY id")
    suspend fun getAll(): List<ConsultationEntity>

    @Query("SELECT * FROM consultations WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ConsultationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(consultation: ConsultationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(consultations: List<ConsultationEntity>)

    @Update
    suspend fun update(consultation: ConsultationEntity)

    @Delete
    suspend fun delete(consultation: ConsultationEntity)

    @Query("DELETE FROM consultations WHERE id = :id")
    suspend fun deleteById(id: Int)
}
