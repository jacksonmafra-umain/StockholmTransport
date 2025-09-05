package com.umain.transport.departures.domain.repository

import com.umain.transport.departures.domain.model.Departure

interface DeparturesRepository {
    suspend fun getDeparturesForSite(siteId: Int): Result<List<Departure>>
}