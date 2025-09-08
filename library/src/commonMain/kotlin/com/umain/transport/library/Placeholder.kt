package com.umain.transport.library

/**
 * This is an empty placeholder class.
 * Its sole purpose is to ensure that the Kotlin compiler runs for the ':library' module,
 * generating the necessary artifacts (.klib files) that the 'maven-publish' plugin requires.
 * Without this file, Gradle might optimize the build by skipping compilation for this
 * "empty" module, causing a FileNotFoundException during the publishing task.
 */
internal object StockholmTransportLibrary