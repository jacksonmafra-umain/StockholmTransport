package com.umain.transport.core.presentation

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
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
    @JsExport.Ignore
    protected val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /**
     * This must be implemented by subclasses to expose their StateFlow.
     * It is not exported to JS as StateFlow is not JS-compatible — JS
     * consumers go through [subscribe] instead. Kotlin consumers (Compose,
     * SwiftUI bridges, JVM tests) collect this directly.
     */
    @JsExport.Ignore
    open val uiState: StateFlow<T>? = null

    /**
     * A JS-friendly way to subscribe to state updates.
     * JavaScript clients will call this method with a callback function.
     *
     * Returns a [Subscription] handle so the caller can cancel this single
     * subscription without tearing down the whole ViewModel. Calling
     * [onCleared] still cancels every running subscription via the scope.
     *
     * @param onStateUpdate The callback function to be invoked with the new state.
     * @return A cancellation handle. Calling `cancel()` cancels only this
     *         subscription; existing JS callers that ignored the return value
     *         continue to work unchanged — `onCleared()` cleans up everything.
     */
    fun subscribe(onStateUpdate: (T) -> Unit): Subscription {
        val job = viewModelScope.launch {
            uiState?.collect {
                onStateUpdate(it)
            }
        }
        return Subscription { job.cancel() }
    }

    /**
     * This should be called when the ViewModel is no longer needed to cancel
     * all running coroutines and prevent memory leaks.
     */
    open fun onCleared() {
        viewModelScope.cancel()
    }
}

/**
 * A cancellation handle returned by [BaseViewModel.subscribe]. Exposed as a
 * `fun interface` so it's emitted as a TypeScript-friendly interface
 * (`{ cancel(): void }`) instead of a Kotlin class.
 */
@JsExport
fun interface Subscription {
    fun cancel()
}
