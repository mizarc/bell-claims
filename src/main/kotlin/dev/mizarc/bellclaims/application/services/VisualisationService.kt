package dev.mizarc.bellclaims.application.services

import dev.mizarc.bellclaims.domain.values.Area
import dev.mizarc.bellclaims.domain.values.Position2D
import dev.mizarc.bellclaims.domain.values.Position3D
import java.util.UUID

interface VisualisationService {
    fun displayComplete(playerId: UUID, areas: Set<Area>, edgeBlock: String): Set<Position3D>
    fun displayPartitioned(playerId: UUID, areas: Set<Area>, edgeBlock: String, cornerBlock: String): Set<Position3D>
    fun clear(playerId: UUID, areas: Set<Area>): Set<Position3D>
    fun clearAll(playerId: UUID): Set<Position2D>
}