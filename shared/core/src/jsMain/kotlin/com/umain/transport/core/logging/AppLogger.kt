package com.umain.transport.core.logging

actual object AppLogger {
    actual fun e(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        console.error("[$tag] $message", throwable ?: "")
    }

    actual fun d(
        tag: String,
        message: String,
    ) {
        console.log("[$tag] $message")
    }

    actual fun i(
        tag: String,
        message: String,
    ) {
        console.info("[$tag] $message")
    }
}
