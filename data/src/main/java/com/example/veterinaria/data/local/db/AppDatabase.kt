package com.example.veterinaria.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.veterinaria.data.local.dao.AppointmentDao
import com.example.veterinaria.data.local.dao.ConsultationDao
import com.example.veterinaria.data.local.dao.OwnerDao
import com.example.veterinaria.data.local.dao.PetDao
import com.example.veterinaria.data.local.dao.UserDao
import com.example.veterinaria.data.local.entity.AppointmentEntity
import com.example.veterinaria.data.local.entity.ConsultationEntity
import com.example.veterinaria.data.local.entity.OwnerEntity
import com.example.veterinaria.data.local.entity.PetEntity
import com.example.veterinaria.data.local.entity.UserEntity

@Database(
    entities = [
        OwnerEntity::class,
        PetEntity::class,
        ConsultationEntity::class,
        AppointmentEntity::class,
        UserEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ownerDao(): OwnerDao
    abstract fun petDao(): PetDao
    abstract fun consultationDao(): ConsultationDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun userDao(): UserDao
}
