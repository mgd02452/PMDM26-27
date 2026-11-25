package com.example.conectividad

import java.io.IOException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>
    data class HttpError(val code: Int, val message: String) : NetworkResult<Nothing>
    data class NetworkError(val cause: Exception) : NetworkResult<Nothing>
    data class SerializationError(val cause: Exception) : NetworkResult<Nothing>
}

suspend fun <T> safeApiCall(block: suspend () -> T): NetworkResult<T> {
    return try {
        NetworkResult.Success(block())
    } catch (e: HttpException) {          // El servidor respondió 4xx / 5xx
        NetworkResult.HttpError(e.code(), e.message())
    } catch (e: IOException) {            // Sin red, DNS, timeout...
        NetworkResult.NetworkError(e)
    } catch (e: SerializationException) { // El JSON no encaja con el DTO
        NetworkResult.SerializationError(e)
    }
}