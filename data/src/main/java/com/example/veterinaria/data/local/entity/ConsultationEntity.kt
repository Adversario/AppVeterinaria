package com.example.veterinaria.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "consultations",
    foreignKeys = [
        ForeignKey(
            entity = PetEntity::class,
            parentColumns = ["id"],
            childColumns = ["petId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("petId")]
)
data class ConsultationEntity(
    @PrimaryKey val id: String,
    val petId: String,
    val reason: String,
    val date: String,
    val diagnosis: String = "Pendiente",
    val treatment: String = "Pendiente"
)
