package dev.mizarc.bellclaims.application.services

import dev.mizarc.bellclaims.domain.values.Position2D
import dev.mizarc.bellclaims.domain.values.Position3D
import java.util.UUID

interface VisualisationService {
    fun displayComplete(playerId: UUID, edgePositions: Set<Position2D>): Set<Position3D>
    fun displayPartitioned(playerId: UUID, edgePositions: Set<Position2D>,
                           cornerPositions: Set<Position2D>): Set<Position3D>
    fun displayOthers(playerId: UUID, edgePositions: Set<Position2D>): Set<Position3D>
    fun clear(playerId: UUID, edgePositions: Set<Position2D>): Set<Position3D>
    fun clearAll(playerId: UUID): Set<Position2D>
}