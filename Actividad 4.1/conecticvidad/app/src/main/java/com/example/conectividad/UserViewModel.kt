package com.example.conectividad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UiState {
    data object Loading : UiState
    data class Success(val users: List<User>) : UiState
    data class Error(val message: String) : UiState
}

class UserViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { fetchUsers() }

    fun fetchUsers() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = safeApiCall { RetrofitClient.api.getUsers() }) {
                is NetworkResult.Success -> _uiState.value = UiState.Success(result.data)
                is NetworkResult.HttpError -> _uiState.value = UiState.Error("Error del servidor: ${result.code}")
                is NetworkResult.NetworkError -> _uiState.value = UiState.Error("Sin conexión. Revisa tu red.")
                is NetworkResult.SerializationError -> _uiState.value = UiState.Error("Respuesta inesperada del servidor.")
            }
        }
    }
}