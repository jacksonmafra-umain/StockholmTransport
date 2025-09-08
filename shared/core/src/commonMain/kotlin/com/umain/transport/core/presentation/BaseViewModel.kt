package com.umain.transport.core.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * A base class for all ViewModels in the library.
 * It handles the CoroutineScope lifecycle and provides a common
 * way to subscribe to state updates from JavaScript.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
open class BaseViewModel<T> {
    /**
     * The CoroutineScope for this ViewModel. It uses a SupervisorJob
     * to ensure that failures in one coroutine do not cancel the entire scope.
     */
    protected val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /**
     * This must be implemented by subclasses to expose their StateFlow.
     * It is not exported to JS as StateFlow is not JS-compatible.
     */
    protected open val uiState: StateFlow<T>? = null

    /**
     * A JS-friendly way to subscribe to state updates.
     * JavaScript clients will call this method with a callback function.
     *
     * @param onStateUpdate The callback function to be invoked with the new state.
     */
    fun subscribe(onStateUpdate: (T) -> Unit) {
        viewModelScope.launch {
            uiState?.collect {
                onStateUpdate(it)
            }
        }
    }

    /**
     * This should be called when the ViewModel is no longer needed to cancel
     * all running coroutines and prevent memory leaks.
     */
    open fun onCleared() {
        viewModelScope.cancel()
    }
}