package com.example.veterinaria.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.veterinaria.data.local.entity.OwnerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OwnerDao {
    @Query("SELECT * FROM owners ORDER BY id")
    fun observeAll(): Flow<List<OwnerEntity>>

    @Query("SELECT * FROM owners ORDER BY id")
    suspend fun getAll(): List<OwnerEntity>

    @Query("SELECT COUNT(*) FROM owners")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(owner: OwnerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(owners: List<OwnerEntity>)

    @Update
    suspend fun update(owner: OwnerEntity)

    @Delete
    suspend fun delete(owner: OwnerEntity)

    @Query("DELETE FROM owners WHERE id = :id")
    suspend fun deleteById(id: String)
}
