package com.umain.transport.stoppoints.domain.repository

import com.umain.transport.stoppoints.domain.model.StopPoint

interface StopPointsRepository {
    suspend fun getAllStopPoints(): Result<List<StopPoint>>
}