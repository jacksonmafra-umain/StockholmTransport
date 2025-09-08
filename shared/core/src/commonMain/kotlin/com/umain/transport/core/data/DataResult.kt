package com.umain.transport.core.data

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
sealed class DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>()
    data class Error(val error: NetworkError) : DataResult<Nothing>()
}

@OptIn(ExperimentalJsExport::class)
@JsExport
sealed class NetworkError {
    data object NoInternet : NetworkError()
    data object Timeout : NetworkError()
    data object NotFound : NetworkError()
    data object ServerError : NetworkError()
    data class Unknown(val message: String) : NetworkError()
}