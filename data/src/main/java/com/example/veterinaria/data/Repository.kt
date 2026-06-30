package com.example.veterinaria.data

import android.content.Context
import androidx.room.Room
import com.example.veterinaria.data.local.db.AppDatabase
import com.example.veterinaria.data.local.entity.AppointmentEntity
import com.example.veterinaria.data.local.entity.ConsultationEntity
import com.example.veterinaria.data.local.entity.OwnerEntity
import com.example.veterinaria.data.local.entity.PetEntity
import com.example.veterinaria.data.local.entity.UserEntity
import com.example.veterinaria.data.local.session.SessionPreferences
import com.example.veterinaria.data.local.toDomain
import com.example.veterinaria.data.model.ActivityEvent
import com.example.veterinaria.data.model.Cita
import com.example.veterinaria.data.model.Consulta
import com.example.veterinaria.data.model.Dueno
import com.example.veterinaria.data.model.Mascota
import com.example.veterinaria.data.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object Repository {

    @Volatile
    private var database: AppDatabase? = null

    @Volatile
    private var sessionPreferences: SessionPreferences? = null

    private val db: AppDatabase
        get() = checkNotNull(database) {
            "Repository.init(context) debe llamarse antes de usar el repositorio."
        }

    private val sessions: SessionPreferences
        get() = checkNotNull(sessionPreferences) {
            "Repository.init(context) debe llamarse antes de usar la sesión."
        }

    private val _activityLog = MutableStateFlow<List<ActivityEvent>>(emptyList())
    val activityLog: StateFlow<List<ActivityEvent>> = _activityLog

    val sessionFlow: Flow<Session?>
        get() = sessions.session

    val duenosFlow: Flow<List<Dueno>>
        get() = db.ownerDao().observeAll().map { rows -> rows.map { it.toDomain() } }

    val mascotasFlow: Flow<List<Mascota>>
        get() = db.petDao().observeAll().map { rows -> rows.map { it.toDomain() } }

    val consultasFlow: Flow<List<Consulta>>
        get() = db.consultationDao().observeAll().map { rows -> rows.map { it.toDomain() } }

    val citasFlow: Flow<List<Cita>>
        get() = db.appointmentDao().observeAll().map { rows -> rows.map { it.toDomain() } }

    fun init(context: Context) {
        if (database != null && sessionPreferences != null) return

        synchronized(this) {
            if (database == null) {
                val appContext = context.applicationContext
                database = Room.databaseBuilder(
                    appContext,
                    AppDatabase::class.java,
                    "veterinaria.db"
                ).fallbackToDestructiveMigration().build()
                sessionPreferences = SessionPreferences(appContext)
                runBlocking { seedIfNeeded() }
            }
        }
    }

    private suspend fun seedIfNeeded() {
        if (db.ownerDao().count() > 0) return

        db.ownerDao().insertAll(
            listOf(
                OwnerEntity(id = "owner-ana", name = "Ana Soto", phone = "+56 9 1234 5678", email = "ana.soto@vet-demo.cl"),
                OwnerEntity(id = "owner-carlos", name = "Carlos Ruiz", phone = "+56 9 8765 4321", email = "carlos.ruiz@vet-demo.cl"),
                OwnerEntity(id = "owner-daniela", name = "Daniela Pérez", phone = "+56 9 2222 3333")
            )
        )

        db.petDao().insertAll(
            listOf(
                PetEntity(id = "pet-bruno", ownerId = "owner-ana", name = "Bruno", species = "Perro", breed = "Mestizo", age = 4),
                PetEntity(id = "pet-mishi", ownerId = "owner-carlos", name = "Mishi", species = "Gato", breed = "Europeo", age = 2),
                PetEntity(id = "pet-lola", ownerId = "owner-daniela", name = "Lola", species = "Conejo", breed = "Enano", age = 1)
            )
        )

        db.consultationDao().insertAll(
            listOf(
                ConsultationEntity(
                    id = "consultation-control-bruno",
                    petId = "pet-bruno",
                    reason = "Control",
                    date = "2025-11-10",
                    diagnosis = "OK",
                    treatment = "N/A"
                ),
                ConsultationEntity(
                    id = "consultation-vacuna-mishi",
                    petId = "pet-mishi",
                    reason = "Vacuna",
                    date = "2025-11-22",
                    diagnosis = "Vacunación",
                    treatment = "Dosis aplicada"
                ),
                ConsultationEntity(
                    id = "consultation-chequeo-lola",
                    petId = "pet-lola",
                    reason = "Chequeo",
                    date = "2025-11-28",
                    diagnosis = "OK",
                    treatment = "N/A"
                )
            )
        )

        db.userDao().insertAll(
            listOf(
                UserEntity(id = "user-staff", email = "staff@vet.cl", password = "1234", role = "STAFF", ownerId = null),
                UserEntity(id = "user-owner-ana", email = "owner1@vet.cl", password = "1234", role = "OWNER", ownerId = "owner-ana")
            )
        )
    }

    private fun nowLabel(): String =
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

    private fun push(tipo: String, msg: String) {
        val ev = ActivityEvent(tipo = tipo, mensaje = msg, epochMs = System.currentTimeMillis(), hora = nowLabel())
        _activityLog.value = (listOf(ev) + _activityLog.value).take(200)
    }

    fun logAuth(msg: String) = push("AUTH", msg)
    fun logCrud(msg: String) = push("CRUD", msg)
    fun logNotif(msg: String) = push("NOTIF", msg)
    fun logError(msg: String) = push("ERROR", msg)

    fun login(email: String, password: String): Session = runBlocking {
        val user = db.userDao().findByEmail(email) ?: throw IllegalArgumentException("User not found")
        if (user.password != password) throw IllegalArgumentException("Bad password")

        Session(email = user.email, rol = user.role, ownerId = user.ownerId).also { sessions.save(it) }
    }

    fun logout() = runBlocking {
        sessions.clear()
    }

    fun resetPassword(email: String): String = runBlocking {
        val user = db.userDao().findByEmail(email) ?: throw IllegalArgumentException("User not found")
        val newPass = "VET-${(1000..9999).random()}"
        db.userDao().update(user.copy(password = newPass))
        newPass
    }

    fun getDuenos(): List<Dueno> = runBlocking {
        db.ownerDao().getAll().map { it.toDomain() }
    }

    fun getMascotas(): List<Mascota> = runBlocking {
        db.petDao().getAll().map { it.toDomain() }
    }

    fun getConsultas(): List<Consulta> = runBlocking {
        db.consultationDao().getAll().map { it.toDomain() }
    }

    fun getCitas(): List<Cita> = runBlocking {
        db.appointmentDao().getAll().map { it.toDomain() }
    }

    fun addDueno(nombre: String, telefono: String, email: String? = null): Dueno = runBlocking {
        val id = newId()
        val cleanEmail = email?.trim()?.takeIf { it.isNotBlank() }
        db.ownerDao().insert(OwnerEntity(id = id, name = nombre, phone = telefono, email = cleanEmail))
        val owner = Dueno(id = id, nombre = nombre, telefono = telefono, email = cleanEmail)

        val ownerEmail = cleanEmail ?: "owner${owner.id}@vet.cl"
        db.userDao().insert(
            UserEntity(
                id = newId(),
                email = ownerEmail,
                password = "1234",
                role = "OWNER",
                ownerId = owner.id
            )
        )
        push("AUTH", "Se creó acceso OWNER demo: $ownerEmail / 1234 (ownerId=${owner.id})")
        owner
    }

    fun updateDueno(id: String, nombre: String, telefono: String, email: String?) = runBlocking {
        val cleanEmail = email?.trim()?.takeIf { it.isNotBlank() }
        db.ownerDao().update(OwnerEntity(id = id, name = nombre, phone = telefono, email = cleanEmail))
    }

    fun deleteDueno(id: String) = runBlocking {
        db.ownerDao().deleteById(id)
    }

    fun addMascota(
        duenoId: String,
        nombre: String,
        especie: String,
        raza: String = "N/A",
        edad: Int = 0
    ): Mascota = runBlocking {
        val id = newId()
        db.petDao().insert(
            PetEntity(
                id = id,
                ownerId = duenoId,
                name = nombre,
                species = especie,
                breed = raza,
                age = edad
            )
        )
        Mascota(id = id, duenoId = duenoId, nombre = nombre, especie = especie, raza = raza, edad = edad)
    }

    fun deleteMascota(id: String) = runBlocking {
        db.petDao().deleteById(id)
    }

    fun addConsulta(
        mascotaId: String,
        motivo: String,
        fecha: String,
        diagnostico: String = "Pendiente",
        tratamiento: String = "Pendiente"
    ): Consulta = runBlocking {
        val id = newId()
        db.consultationDao().insert(
            ConsultationEntity(
                id = id,
                petId = mascotaId,
                reason = motivo,
                date = fecha,
                diagnosis = diagnostico,
                treatment = tratamiento
            )
        )
        Consulta(
            id = id,
            mascotaId = mascotaId,
            motivo = motivo,
            fecha = fecha,
            diagnostico = diagnostico,
            tratamiento = tratamiento
        )
    }

    fun deleteConsulta(id: String) = runBlocking {
        db.consultationDao().deleteById(id)
    }

    fun updateConsulta(
        id: String,
        motivo: String,
        fecha: String,
        diagnostico: String,
        tratamiento: String
    ) = runBlocking {
        val old = db.consultationDao().getById(id) ?: return@runBlocking
        db.consultationDao().update(
            old.copy(
                reason = motivo,
                date = fecha,
                diagnosis = diagnostico,
                treatment = tratamiento
            )
        )
    }

    fun addCita(mascotaId: String, fecha: String, nota: String): Cita = runBlocking {
        val id = newId()
        db.appointmentDao().insert(
            AppointmentEntity(
                id = id,
                petId = mascotaId,
                date = fecha,
                note = nota
            )
        )
        Cita(id = id, mascotaId = mascotaId, fecha = fecha, nota = nota)
    }

    fun deleteCita(id: String) = runBlocking {
        db.appointmentDao().deleteById(id)
    }

    private fun newId(): String = UUID.randomUUID().toString()
}
