package com.example.veterinariaapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.veterinaria.data.Repository
import com.example.veterinaria.data.model.ActivityEvent
import com.example.veterinaria.data.model.Cita
import com.example.veterinaria.data.model.Consulta
import com.example.veterinaria.data.model.Dueno
import com.example.veterinaria.data.model.Mascota
import com.example.veterinariaapp.platform.ReminderWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private data class VetSnapshot(
    val duenos: List<Dueno>,
    val mascotas: List<Mascota>,
    val consultas: List<Consulta>,
    val citas: List<Cita>
)

class VetViewModel(app: Application) : AndroidViewModel(app) {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _totalMascotas = MutableLiveData(0)
    val totalMascotas: LiveData<Int> = _totalMascotas

    private val _totalConsultas = MutableLiveData(0)
    val totalConsultas: LiveData<Int> = _totalConsultas

    private val _ultimoDuenoNombre = MutableLiveData("-")
    val ultimoDuenoNombre: LiveData<String> = _ultimoDuenoNombre

    private val _duenos = MutableLiveData<List<Dueno>>(emptyList())
    val duenos: LiveData<List<Dueno>> = _duenos

    private val _mascotas = MutableLiveData<List<Mascota>>(emptyList())
    val mascotas: LiveData<List<Mascota>> = _mascotas

    private val _consultas = MutableLiveData<List<Consulta>>(emptyList())
    val consultas: LiveData<List<Consulta>> = _consultas

    private val _citas = MutableLiveData<List<Cita>>(emptyList())
    val citas: LiveData<List<Cita>> = _citas

    val activityLog: StateFlow<List<ActivityEvent>> = Repository.activityLog

    val proximasCitas: StateFlow<List<Cita>>

    init {
        Repository.init(app)
        proximasCitas = Repository.proximasCitasFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
        observeLocalData()
    }

    private fun observeLocalData() {
        viewModelScope.launch {
            combine(
                Repository.duenosFlow,
                Repository.mascotasFlow,
                Repository.consultasFlow,
                Repository.citasFlow
            ) { dsDuenos, dsMascotas, dsConsultas, dsCitas ->
                VetSnapshot(dsDuenos, dsMascotas, dsConsultas, dsCitas)
            }.collect { snapshot ->
                publishData(snapshot.duenos, snapshot.mascotas, snapshot.consultas, snapshot.citas)
            }
        }
    }

    private fun publishData(
        dsDuenos: List<Dueno>,
        dsMascotas: List<Mascota>,
        dsConsultas: List<Consulta>,
        dsCitas: List<Cita>
    ) {
        _duenos.postValue(dsDuenos)
        _mascotas.postValue(dsMascotas)
        _consultas.postValue(dsConsultas)
        _citas.postValue(dsCitas)

        _totalMascotas.postValue(dsMascotas.size)
        _totalConsultas.postValue(dsConsultas.size)
        _ultimoDuenoNombre.postValue(dsDuenos.maxByOrNull { it.id }?.nombre ?: "-")
    }

    fun cargarResumenConProgreso() {
        _isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            delay(300)

            val dsDuenos = Repository.getDuenos()
            val dsMascotas = Repository.getMascotas()
            val dsConsultas = Repository.getConsultas()
            val dsCitas = Repository.getCitas()

            publishData(dsDuenos, dsMascotas, dsConsultas, dsCitas)
            _isLoading.postValue(false)
        }
    }

    fun agregarDueno(nombre: String, telefono: String, email: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            Repository.addDueno(nombre, telefono, email)
            Repository.logCrud("Crear dueno: $nombre")
            cargarResumenConProgreso()
        }
    }

    fun editarDueno(id: String, nombre: String, telefono: String, email: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            Repository.updateDueno(id, nombre, telefono, email)
            Repository.logCrud("Editar dueno: $nombre")
            cargarResumenConProgreso()
        }
    }

    fun eliminarDueno(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Repository.deleteDueno(id)
            Repository.logCrud("Eliminar dueno")
            cargarResumenConProgreso()
        }
    }

    fun agregarMascota(duenoId: String, nombre: String, especie: String, raza: String, edad: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            Repository.addMascota(duenoId, nombre, especie, raza, edad)
            Repository.logCrud("Crear mascota: $nombre")
            cargarResumenConProgreso()
        }
    }

    fun eliminarMascota(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Repository.deleteMascota(id)
            Repository.logCrud("Eliminar mascota")
            cargarResumenConProgreso()
        }
    }

    fun editarMascota(id: String, duenoId: String, nombre: String, especie: String, raza: String, edad: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            Repository.updateMascota(id, duenoId, nombre, especie, raza, edad)
            Repository.logCrud("Editar mascota: $nombre")
            cargarResumenConProgreso()
        }
    }

    fun agregarConsulta(
        mascotaId: String,
        motivo: String,
        fecha: String,
        diagnostico: String,
        tratamiento: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            Repository.addConsulta(mascotaId, motivo, fecha, diagnostico, tratamiento)
            Repository.logCrud("Crear consulta: $motivo ($fecha)")
            cargarResumenConProgreso()
        }
    }

    fun editarConsulta(id: String, motivo: String, fecha: String, diagnostico: String, tratamiento: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Repository.updateConsulta(id, motivo, fecha, diagnostico, tratamiento)
            Repository.logCrud("Editar consulta: $motivo")
            cargarResumenConProgreso()
        }
    }

    fun eliminarConsulta(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Repository.deleteConsulta(id)
            Repository.logCrud("Eliminar consulta")
            cargarResumenConProgreso()
        }
    }

    fun agendarCita(mascotaId: String, fecha: String, nota: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val cita = Repository.addCita(mascotaId, fecha, nota)
            Repository.logCrud("Agendar cita: $nota ($fecha)")

            programarNotificacionDemo(cita)

            cargarResumenConProgreso()
        }
    }

    fun eliminarCita(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Repository.deleteCita(id)
            Repository.logCrud("Eliminar cita")
            cargarResumenConProgreso()
        }
    }

    private fun programarNotificacionDemo(cita: Cita) {
        val ctx = getApplication<Application>()
        val data = workDataOf(
            "title" to "Recordatorio de cita",
            "message" to "Cita proxima (${cita.fecha})"
        )
        val req = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(ctx).enqueue(req)
        Repository.logNotif("Programada notificacion demo para cita")
    }
}
