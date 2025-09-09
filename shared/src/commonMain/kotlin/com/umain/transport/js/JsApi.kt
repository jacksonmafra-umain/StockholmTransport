package com.umain.transport.js

import com.umain.transport.authorities.presentation.AuthoritiesViewModel
import com.umain.transport.departures.presentation.DeparturesViewModel
import com.umain.transport.lines.presentation.LinesViewModel
import com.umain.transport.sites.presentation.SitesViewModel
import com.umain.transport.stoppoints.presentation.StopPointsViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.js.JsExport
import kotlin.js.JsName

/**
 * This object serves as the public API for JavaScript consumers.
 * It implements KoinComponent to get direct access to the dependency graph.
 */
@JsExport
object StockholmTransportApi : KoinComponent {

    @JsName("getLinesViewModel")
    fun getLinesViewModel(): LinesViewModel = get()

    @JsName("getSitesViewModel")
    fun getSitesViewModel(): SitesViewModel = get()

    @JsName("getDeparturesViewModel")
    fun getDeparturesViewModel(): DeparturesViewModel = get()

    @JsName("getStopPointsViewModel")
    fun getStopPointsViewModel(): StopPointsViewModel = get()

    @JsName("getAuthoritiesViewModel")
    fun getAuthoritiesViewModel(): AuthoritiesViewModel = get()
}