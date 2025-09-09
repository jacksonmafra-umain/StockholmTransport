package com.umain.transport.js

import com.umain.transport.authorities.presentation.AuthoritiesViewModel
import com.umain.transport.di.initKoin
import com.umain.transport.departures.presentation.DeparturesViewModel
import com.umain.transport.lines.presentation.LinesViewModel
import com.umain.transport.sites.presentation.SitesViewModel
import com.umain.transport.stoppoints.presentation.StopPointsViewModel
import org.koin.core.Koin
import kotlin.js.JsExport
import kotlin.js.JsName

/**
 * This is the single, public entry point for all JavaScript consumers.
 */
@JsExport
object StockholmTransportApi {
    private var koin: Koin? = null

    /**
     * Initializes the Koin dependency injection container.
     * This must be called once before any other functions are used.
     */
    @JsName("initialize")
    fun initialize() {
        val koinApplication = initKoin()
        koin = koinApplication.koin
    }

    private fun <T : Any> getKoinInstance(clazz: kotlin.reflect.KClass<T>): T {
        val koinInstance = koin
            ?: error("Koin has not been initialized. Please call StockholmTransportApi.initialize() first.")
        return koinInstance.get(clazz, null, null)
    }

    @JsName("getLinesViewModel")
    fun getLinesViewModel(): LinesViewModel = getKoinInstance(LinesViewModel::class)

    @JsName("getSitesViewModel")
    fun getSitesViewModel(): SitesViewModel = getKoinInstance(SitesViewModel::class)

    @JsName("getDeparturesViewModel")
    fun getDeparturesViewModel(): DeparturesViewModel = getKoinInstance(DeparturesViewModel::class)

    @JsName("getStopPointsViewModel")
    fun getStopPointsViewModel(): StopPointsViewModel = getKoinInstance(StopPointsViewModel::class)

    @JsName("getAuthoritiesViewModel")
    fun getAuthoritiesViewModel(): AuthoritiesViewModel = getKoinInstance(AuthoritiesViewModel::class)
}