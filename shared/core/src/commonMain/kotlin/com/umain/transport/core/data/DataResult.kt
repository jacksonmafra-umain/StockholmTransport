package com.umain.transport.core.data

sealed class DataResult<out T> {
    data class Success<T>(
        val data: T,
    ) : DataResult<T>()

    data class Error(
        val error: NetworkError,
    ) : DataResult<Nothing>()
}

sealed class NetworkError {
    data object NoInternet : NetworkError()

    data object ServerError : NetworkError()

    data object Timeout : NetworkError()

    data class Unknown(
        val message: String,
    ) : NetworkError()
}
