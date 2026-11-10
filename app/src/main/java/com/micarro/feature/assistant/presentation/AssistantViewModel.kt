package com.micarro.feature.assistant.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micarro.domain.model.Vehicle
import com.micarro.domain.repository.MaintenanceRepository
import com.micarro.domain.repository.PartRepository
import com.micarro.domain.repository.VehicleRepository
import com.micarro.feature.assistant.data.AssistantTurn
import com.micarro.feature.assistant.data.GeminiAssistantClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AssistantMessage(val fromUser: Boolean, val text: String)

data class AssistantUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val selectedVehicle: Vehicle? = null,
    val messages: List<AssistantMessage> = emptyList(),
    val draft: String = "",
    val sending: Boolean = false,
    val configured: Boolean = true
)

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val partRepository: PartRepository,
    private val client: GeminiAssistantClient
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val _uiState = MutableStateFlow(AssistantUiState(configured = client.isConfigured))
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            vehicleRepository.observeVehicles().collect { vehicles ->
                _uiState.update { state ->
                    val selected = state.selectedVehicle?.let { current ->
                        vehicles.find { it.id == current.id }
                    } ?: vehicles.find { it.isPrimary } ?: vehicles.firstOrNull()
                    state.copy(vehicles = vehicles, selectedVehicle = selected)
                }
            }
        }
    }

    fun selectVehicle(vehicle: Vehicle) {
        _uiState.update { it.copy(selectedVehicle = vehicle, messages = emptyList()) }
    }

    fun updateDraft(value: String) {
        _uiState.update { it.copy(draft = value) }
    }

    fun send() {
        val state = _uiState.value
        val question = state.draft.trim()
        val vehicle = state.selectedVehicle
        if (question.isEmpty() || state.sending) return
        if (!client.isConfigured) {
            _uiState.update { it.copy(configured = false) }
            return
        }
        val history = state.messages
        _uiState.update {
            it.copy(
                draft = "",
                sending = true,
                messages = it.messages + AssistantMessage(fromUser = true, text = question)
            )
        }
        viewModelScope.launch {
            val answer = runCatching {
                client.reply(
                    history = history.map { AssistantTurn(it.fromUser, it.text) },
                    context = vehicle?.let { contextOf(it) }
                        ?: "No hay un vehículo registrado en la app.",
                    question = question
                )
            }.getOrElse { error ->
                error.message ?: "No se pudo consultar al asistente"
            }
            _uiState.update {
                it.copy(
                    sending = false,
                    messages = it.messages + AssistantMessage(fromUser = false, text = answer)
                )
            }
        }
    }

    private suspend fun contextOf(vehicle: Vehicle): String {
        val vehicleId = vehicle.id.toString()
        val services = maintenanceRepository.observeServices(vehicleId).first()
            .sortedByDescending { it.date }
            .take(20)
        val parts = partRepository.observeInstalledParts(vehicleId).first().take(40)
        return buildString {
            appendLine(
                "${vehicle.brand} ${vehicle.line} ${vehicle.model} ${vehicle.year}, " +
                    "placa ${vehicle.plate}, ${vehicle.currentMileage} km, tipo ${vehicle.type}"
            )
            vehicle.fuelType?.let { appendLine("Combustible: $it") }
            if (services.isEmpty()) {
                appendLine("Servicios registrados: ninguno.")
            } else {
                appendLine("Servicios registrados, del más reciente al más antiguo:")
                services.forEach { service ->
                    append("- ")
                    append(dateFormat.format(Date(service.date)))
                    append(" · ")
                    append(service.title)
                    append(" · ")
                    append(service.category)
                    append(" · ")
                    append(service.serviceType)
                    append(" · ")
                    append(service.mileage)
                    append(" km")
                    if (service.description.isNotBlank()) {
                        append(" · ")
                        append(service.description)
                    }
                    if (service.workshopName.isNotBlank()) {
                        append(" · taller ")
                        append(service.workshopName)
                    }
                    appendLine()
                }
            }
            if (parts.isEmpty()) {
                appendLine("Repuestos instalados: ninguno.")
            } else {
                appendLine("Repuestos instalados:")
                parts.forEach { part ->
                    append("- ")
                    append(part.name)
                    part.brand?.let { append(" · marca $it") }
                    part.installationDate?.let { append(" · instalado ${dateFormat.format(Date(it))}") }
                    part.originTitle?.let { append(" · en $it") }
                    part.notes?.takeIf { it.isNotBlank() }?.let { append(" · $it") }
                    appendLine()
                }
            }
        }
    }
}
