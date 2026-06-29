package com.example.veterinariaapp.viewmodel

import android.app.Application
import androidx.lifecycle.*
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.veterinaria.data.Repository
import com.example.veterinaria.data.model.*
import com.example.veterinariaapp.platform.ReminderWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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

    init {
        Repository.init(app)
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

    fun agregarDueno(nombre: String, telefono: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Repository.addDueno(nombre, telefono)
            Repository.logCrud("Crear dueño: $nombre")
            cargarResumenConProgreso()
        }
    }

    fun agregarMascota(duenoId: Int, nombre: String, especie: String, raza: String, edad: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            Repository.addMascota(duenoId, nombre, especie, raza, edad)
            Repository.logCrud("Crear mascota: $nombre (dueñoId=$duenoId)")
            cargarResumenConProgreso()
        }
    }

    fun eliminarMascota(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            Repository.deleteMascota(id)
            Repository.logCrud("Eliminar mascota id=$id")
            cargarResumenConProgreso()
        }
    }

    fun agregarConsulta(mascotaId: Int, motivo: String, fecha: String, diagnostico: String, tratamiento: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Repository.addConsulta(mascotaId, motivo, fecha, diagnostico, tratamiento)
            Repository.logCrud("Crear consulta mascotaId=$mascotaId ($fecha)")
            cargarResumenConProgreso()
        }
    }

    fun editarConsulta(id: Int, motivo: String, fecha: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Repository.updateConsulta(id, motivo, fecha)
            Repository.logCrud("Editar consulta id=$id")
            cargarResumenConProgreso()
        }
    }

    fun eliminarConsulta(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            Repository.deleteConsulta(id)
            Repository.logCrud("Eliminar consulta id=$id")
            cargarResumenConProgreso()
        }
    }

    fun agendarCita(mascotaId: Int, fecha: String, nota: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val cita = Repository.addCita(mascotaId, fecha, nota)
            Repository.logCrud("Agendar cita id=${cita.id} mascotaId=$mascotaId ($fecha)")

            // Notificación automática (demo): se dispara en 10s
            programarNotificacionDemo(cita)

            cargarResumenConProgreso()
        }
    }

    fun eliminarCita(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            Repository.deleteCita(id)
            Repository.logCrud("Eliminar cita id=$id")
            cargarResumenConProgreso()
        }
    }

    private fun programarNotificacionDemo(cita: Cita) {
        val ctx = getApplication<Application>()
        val data = workDataOf(
            "title" to "Recordatorio de cita",
            "message" to "Cita próxima (${cita.fecha}) • Mascota ID ${cita.mascotaId}"
        )
        val req = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(ctx).enqueue(req)
        Repository.logNotif("Programada notificación (demo) para cita id=${cita.id}")
    }
}
