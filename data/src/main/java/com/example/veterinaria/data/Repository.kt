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
                ).build()
                sessionPreferences = SessionPreferences(appContext)
                runBlocking { seedIfNeeded() }
            }
        }
    }

    private suspend fun seedIfNeeded() {
        if (db.ownerDao().count() > 0) return

        db.ownerDao().insertAll(
            listOf(
                OwnerEntity(id = 1, name = "Ana Soto", phone = "+56 9 1234 5678"),
                OwnerEntity(id = 2, name = "Carlos Ruiz", phone = "+56 9 8765 4321"),
                OwnerEntity(id = 3, name = "Daniela Pérez", phone = "+56 9 2222 3333")
            )
        )

        db.petDao().insertAll(
            listOf(
                PetEntity(id = 1, ownerId = 1, name = "Bruno", species = "Perro", breed = "Mestizo", age = 4),
                PetEntity(id = 2, ownerId = 2, name = "Mishi", species = "Gato", breed = "Europeo", age = 2),
                PetEntity(id = 3, ownerId = 3, name = "Lola", species = "Conejo", breed = "Enano", age = 1)
            )
        )

        db.consultationDao().insertAll(
            listOf(
                ConsultationEntity(
                    id = 1,
                    petId = 1,
                    reason = "Control",
                    date = "2025-11-10",
                    diagnosis = "OK",
                    treatment = "N/A"
                ),
                ConsultationEntity(
                    id = 2,
                    petId = 2,
                    reason = "Vacuna",
                    date = "2025-11-22",
                    diagnosis = "Vacunación",
                    treatment = "Dosis aplicada"
                ),
                ConsultationEntity(
                    id = 3,
                    petId = 3,
                    reason = "Chequeo",
                    date = "2025-11-28",
                    diagnosis = "OK",
                    treatment = "N/A"
                )
            )
        )

        db.userDao().insertAll(
            listOf(
                UserEntity(id = 1, email = "staff@vet.cl", password = "1234", role = "STAFF", ownerId = null),
                UserEntity(id = 2, email = "owner1@vet.cl", password = "1234", role = "OWNER", ownerId = 1)
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

    fun addDueno(nombre: String, telefono: String): Dueno = runBlocking {
        val id = db.ownerDao().insert(OwnerEntity(name = nombre, phone = telefono)).toInt()
        val owner = Dueno(id = id, nombre = nombre, telefono = telefono)

        val ownerEmail = "owner${owner.id}@vet.cl"
        db.userDao().insert(
            UserEntity(
                email = ownerEmail,
                password = "1234",
                role = "OWNER",
                ownerId = owner.id
            )
        )
        push("AUTH", "Se creó acceso OWNER demo: $ownerEmail / 1234 (ownerId=${owner.id})")
        owner
    }

    fun addMascota(
        duenoId: Int,
        nombre: String,
        especie: String,
        raza: String = "N/A",
        edad: Int = 0
    ): Mascota = runBlocking {
        val id = db.petDao().insert(
            PetEntity(
                ownerId = duenoId,
                name = nombre,
                species = especie,
                breed = raza,
                age = edad
            )
        ).toInt()
        Mascota(id = id, duenoId = duenoId, nombre = nombre, especie = especie, raza = raza, edad = edad)
    }

    fun deleteMascota(id: Int) = runBlocking {
        db.petDao().deleteById(id)
    }

    fun addConsulta(
        mascotaId: Int,
        motivo: String,
        fecha: String,
        diagnostico: String = "Pendiente",
        tratamiento: String = "Pendiente"
    ): Consulta = runBlocking {
        val id = db.consultationDao().insert(
            ConsultationEntity(
                petId = mascotaId,
                reason = motivo,
                date = fecha,
                diagnosis = diagnostico,
                treatment = tratamiento
            )
        ).toInt()
        Consulta(
            id = id,
            mascotaId = mascotaId,
            motivo = motivo,
            fecha = fecha,
            diagnostico = diagnostico,
            tratamiento = tratamiento
        )
    }

    fun deleteConsulta(id: Int) = runBlocking {
        db.consultationDao().deleteById(id)
    }

    fun updateConsulta(id: Int, motivo: String, fecha: String) = runBlocking {
        val old = db.consultationDao().getById(id) ?: return@runBlocking
        db.consultationDao().update(old.copy(reason = motivo, date = fecha))
    }

    fun addCita(mascotaId: Int, fecha: String, nota: String): Cita = runBlocking {
        val id = db.appointmentDao().insert(
            AppointmentEntity(
                petId = mascotaId,
                date = fecha,
                note = nota
            )
        ).toInt()
        Cita(id = id, mascotaId = mascotaId, fecha = fecha, nota = nota)
    }

    fun deleteCita(id: Int) = runBlocking {
        db.appointmentDao().deleteById(id)
    }
}
