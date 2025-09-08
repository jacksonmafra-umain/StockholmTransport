package com.umain.transport.app.model

sealed class ModuleItem(val id: String, val title: String) {
    data object Lines : ModuleItem("lines", "Transport Lines")
    data object Sites : ModuleItem("sites", "Sites / Stations")
    data object Departures : ModuleItem("departures", "Departures")
    data object StopPoints : ModuleItem("stoppoints", "Stop Points")
    data object Authorities : ModuleItem("authorities", "Transport Authorities")
}

val allModules = listOf(
    ModuleItem.Lines,
    ModuleItem.Sites,
    ModuleItem.Departures,
    ModuleItem.StopPoints,
    ModuleItem.Authorities
)