package com.peteragurto.riegoapp.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peteragurto.riegoapp.data.DataStoreRepository
import com.peteragurto.riegoapp.data.network.ArduinoApi
import com.peteragurto.riegoapp.data.network.RetrofitHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException

class RiegoViewModel(
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    private val _sensorValue = MutableStateFlow(0)
    val sensorValue: StateFlow<Int> = _sensorValue

    private val _relayState = MutableStateFlow(false)
    val relayState: StateFlow<Boolean> = _relayState

//    private val _isRelayOn = MutableStateFlow(false)
//    val isRelayOn: StateFlow<Boolean> = _isRelayOn

    private val _ipAddress = MutableStateFlow<String?>(null)
    val ipAddress: StateFlow<String?> = _ipAddress

    private var arduinoApi: ArduinoApi? = null

    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var updateJob: Job? = null

    init {
        viewModelScope.launch {
            dataStoreRepository.getIpAddressFlow().collect { ip ->
                _ipAddress.value = ip
                updateArduinoApi(ip)
                checkConnection()
                startPeriodicUpdate()
            }
        }
    }

    private fun updateArduinoApi(ipAddress: String?) {
        arduinoApi = ipAddress?.let { ip ->
            RetrofitHelper.getRetrofitInstance("http://$ip").create(ArduinoApi::class.java)
        }
        Log.d("RiegoViewModel", "Arduino API actualizada: $ipAddress")
    }

    private fun startPeriodicUpdate() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            while (isActive) {
                fetchSensorValue()
                delay(3000)
            }
        }
        Log.d("RiegoViewModel", "Inicio de actualizaciones periódicas.")
    }

    private suspend fun checkConnection() {
        try {
            val response = arduinoApi?.getSensorValue()
            _connectionState.value = response?.isSuccessful == true
            Log.d("RiegoViewModel", "Estado de conexión: ${_connectionState.value}")
        } catch (e: Exception) {
            _connectionState.value = false
            _errorMessage.value = "Error de conexión: ${e.message}"
            Log.e("RiegoViewModel", "Error de conexión: ${e.message}", e)
        }
    }

    fun saveIpAddress(newIpAddress: String) {
        viewModelScope.launch {
            dataStoreRepository.saveIpAddress(newIpAddress)
            // La actualización de _ipAddress y arduinoApi se hará en el collector del init
            Log.d("RiegoViewModel", "Dirección IP guardada: $newIpAddress")
        }
    }

    fun updateRelayState(isOn: Boolean) {
        viewModelScope.launch {
            try {
                val response = if (isOn) arduinoApi?.turnOnRelay() else arduinoApi?.turnOffRelay()
                if (response?.isSuccessful == true) {
                    _relayState.value = isOn
                    Log.d("RiegoViewModel", "Estado del relé actualizado: ${if (isOn) "encendido" else "apagado"}")
                } else {
                    _errorMessage.value = "Error al cambiar el estado del relé: ${response?.code()}"
                    Log.e("RiegoViewModel", "Error al cambiar el estado del relé: ${response?.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cambiar el estado del relé: ${e.message}"
            }
        }
    }

//    fun turnOnRelay() {
//        viewModelScope.launch {
//            try {
//                val response = arduinoApi?.turnOnRelay()
//                if (response?.isSuccessful == true) {
//                    _isRelayOn.value = true
//                }
//            } catch (e: Exception) {
//                _errorMessage.value = "Error al encender el riego: ${e.message}"
//            }
//        }
//    }

//    fun turnOffRelay() {
//        viewModelScope.launch {
//            try {
//                val response = arduinoApi?.turnOffRelay()
//                if (response?.isSuccessful == true) {
//                    _isRelayOn.value = false
//                }
//            } catch (e: Exception) {
//                _errorMessage.value = "Error al apagar el riego: ${e.message}"
//            }
//        }
//    }

    private suspend fun fetchSensorValue() {
        try {
            val response = arduinoApi?.getSensorValue()
            if (response?.isSuccessful == true) {
                val sensorData = response.body()
                _sensorValue.value = sensorData?.sensorValue ?: 0
                _relayState.value = sensorData?.relayState == "on"
                Log.d("RiegoViewModel", "Valor del sensor actualizado: ${_sensorValue.value}")
                Log.d("RiegoViewModel", "Estado del relé: ${_relayState.value}")
            } else {
                _errorMessage.value = "Error del servidor: ${response?.code()}"
                Log.e("RiegoViewModel", "Error del servidor: ${response?.code()}")
            }
        } catch (e: IOException) {
            _errorMessage.value = "Error de red: ${e.message}"
        } catch (e: Exception) {
            _errorMessage.value = "Error inesperado: ${e.message}"
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        updateJob?.cancel()
    }
}