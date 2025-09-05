package com.umain.transport.lines.domain.repository

import com.umain.transport.lines.domain.model.Line
import com.umain.transport.lines.domain.model.TransportMode

interface LinesRepository {
    suspend fun getAllLines(): Result<Map<TransportMode, List<Line>>>
}