package dev.mizarc.bellclaims.application.services

import dev.mizarc.bellclaims.domain.values.Area
import dev.mizarc.bellclaims.domain.values.Position3D
import java.util.UUID

interface VisualisationService {
    fun displayComplete(playerId: UUID, areas: Set<Area>, edgeBlock: String, edgeSurfaceBlock: String): Set<Position3D>
    fun displayPartitioned(playerId: UUID, areas: Set<Area>, edgeBlock: String, edgeSurfaceBlock: String,
                           cornerBlock: String, cornerSurfaceBlock: String): Set<Position3D>
    fun refreshComplete(playerId: UUID, existingPositions: Set<Position3D>, areas: Set<Area>,
                        edgeBlock: String, edgeSurfaceBlock: String): Set<Position3D>
    fun refreshPartitioned(playerId: UUID, existingPositions: Set<Position3D>, areas: Set<Area>, edgeBlock: String,
                           edgeSurfaceBlock: String, cornerBlock: String, cornerSurfaceBlock: String): Set<Position3D>
    fun clear(playerId: UUID, areas: Set<Position3D>)
}