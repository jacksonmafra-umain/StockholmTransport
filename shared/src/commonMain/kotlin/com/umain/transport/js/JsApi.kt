package com.umain.transport.js

import com.umain.transport.authorities.presentation.AuthoritiesViewModel
import com.umain.transport.departures.presentation.DeparturesViewModel
import com.umain.transport.di.initKoin
import com.umain.transport.lines.presentation.LinesViewModel
import com.umain.transport.realtime.RealtimeConfig
import com.umain.transport.realtime.presentation.TripSelectionViewModel
import com.umain.transport.realtime.presentation.TripViewModel
import com.umain.transport.sites.presentation.SitesViewModel
import com.umain.transport.stoppoints.presentation.StopPointsViewModel
import org.koin.core.Koin
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.js.JsStatic

/**
 * Single public entry point for all JavaScript consumers.
 *
 * Implemented as a `class` with a `companion object` whose members are
 * annotated `@JsStatic` so JS callers can use them directly as static methods:
 *
 *     // ESM (named import — preferred)
 *     import { StockholmTransportApi } from '@jacksonmafra-umain/stockholm-transport'
 *     StockholmTransportApi.initialize()
 *
 *     // ESM default import (via `@JsExport.Default`)
 *     import api from '@jacksonmafra-umain/stockholm-transport'
 *     api.initialize()
 *
 * Pre-2.3 Kotlin/JS exported `object` declarations as a class with a static
 * `getInstance()`; the `@JsStatic` companion approach (introduced in 2.3)
 * removes that ceremony — see https://kotlinlang.org/docs/whatsnew23.html
 * and https://kotlinlang.org/docs/js-to-kotlin-interop.html.
 */
@JsExport.Default
@JsExport
class StockholmTransportApi private constructor() {
    companion object {
        private var koin: Koin? = null

        /**
         * Default initialisation — points the realtime data layer at the
         * docker-compose stack on `localhost:3001`. Suitable for the talk's
         * Act 2 React demo running alongside `docker compose up`.
         */
        @JsStatic
        @JsName("initialize")
        fun initialize() {
            val koinApplication = initKoin()
            koin = koinApplication.koin
        }

        /**
         * Initialise with a non-default realtime backend — used when ngrok or
         * a remote simulator has rewritten the simulator URL. Mirrors
         * [com.umain.transport.di.initKoinWithRealtime] but caches the Koin
         * instance for subsequent ViewModel lookups.
         */
        @JsStatic
        @JsName("initializeWithRealtime")
        fun initializeWithRealtime(
            httpBaseUrl: String,
            wsHost: String,
            wsPort: Int,
            wsSecure: Boolean,
        ) {
            val koinApplication = initKoin(
                realtimeConfig = RealtimeConfig(
                    httpBaseUrl = httpBaseUrl,
                    wsHost = wsHost,
                    wsPort = wsPort,
                    wsSecure = wsSecure,
                ),
            )
            koin = koinApplication.koin
        }

        private fun <T : Any> getKoinInstance(clazz: kotlin.reflect.KClass<T>): T {
            val koinInstance = koin
                ?: error("Koin has not been initialized. Please call StockholmTransportApi.initialize() first.")
            return koinInstance.get(clazz, null, null)
        }

        // ----- Static-SDK ViewModels -----

        @JsStatic
        @JsName("getLinesViewModel")
        fun getLinesViewModel(): LinesViewModel = getKoinInstance(LinesViewModel::class)

        @JsStatic
        @JsName("getSitesViewModel")
        fun getSitesViewModel(): SitesViewModel = getKoinInstance(SitesViewModel::class)

        @JsStatic
        @JsName("getDeparturesViewModel")
        fun getDeparturesViewModel(): DeparturesViewModel = getKoinInstance(DeparturesViewModel::class)

        @JsStatic
        @JsName("getStopPointsViewModel")
        fun getStopPointsViewModel(): StopPointsViewModel = getKoinInstance(StopPointsViewModel::class)

        @JsStatic
        @JsName("getAuthoritiesViewModel")
        fun getAuthoritiesViewModel(): AuthoritiesViewModel = getKoinInstance(AuthoritiesViewModel::class)

        // ----- Realtime ViewModels -----

        @JsStatic
        @JsName("getTripViewModel")
        fun getTripViewModel(): TripViewModel = getKoinInstance(TripViewModel::class)

        @JsStatic
        @JsName("getTripSelectionViewModel")
        fun getTripSelectionViewModel(): TripSelectionViewModel = getKoinInstance(TripSelectionViewModel::class)
    }
}
