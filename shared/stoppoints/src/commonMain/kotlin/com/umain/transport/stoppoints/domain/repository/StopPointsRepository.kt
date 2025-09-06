package com.umain.transport.stoppoints.domain.repository

import com.umain.transport.core.data.DataResult
import com.umain.transport.stoppoints.domain.model.StopPoint

interface StopPointsRepository {
    suspend fun getAllStopPoints(): DataResult<List<StopPoint>>
}
