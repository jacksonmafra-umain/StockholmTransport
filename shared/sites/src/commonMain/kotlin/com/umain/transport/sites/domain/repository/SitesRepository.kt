package com.umain.transport.sites.domain.repository

import com.umain.transport.sites.domain.model.Site

interface SitesRepository {
    suspend fun getAllSites(): Result<List<Site>>
}