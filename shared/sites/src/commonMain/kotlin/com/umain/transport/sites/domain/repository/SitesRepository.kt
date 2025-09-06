package com.umain.transport.sites.domain.repository

import com.umain.transport.core.data.DataResult
import com.umain.transport.sites.domain.model.Site

interface SitesRepository {
    suspend fun getAllSites(): DataResult<List<Site>>
}