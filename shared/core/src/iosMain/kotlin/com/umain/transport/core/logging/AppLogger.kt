package com.umain.transport.core.logging

import platform.Foundation.NSLog

actual object AppLogger {
    actual fun e(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        val throwableMessage = throwable?.message?.let { ": $it" } ?: ""
        NSLog("ERROR: [$tag] $message$throwableMessage")
    }

    actual fun d(
        tag: String,
        message: String,
    ) {
        NSLog("DEBUG: [$tag] $message")
    }

    actual fun i(
        tag: String,
        message: String,
    ) {
        NSLog("INFO: [$tag] $message")
    }
}
