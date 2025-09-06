package com.umain.transport.departures.domain.repository

import com.umain.transport.core.data.DataResult
import com.umain.transport.departures.domain.model.Departure

interface DeparturesRepository {
    suspend fun getDeparturesForSite(siteId: Int): DataResult<List<Departure>>
}
