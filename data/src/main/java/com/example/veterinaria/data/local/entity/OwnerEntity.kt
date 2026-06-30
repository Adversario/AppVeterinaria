package com.example.veterinaria.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "owners")
data class OwnerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val email: String? = null
)
