package dev.mizarc.bellclaims.application.services

import dev.mizarc.bellclaims.domain.values.Position2D
import dev.mizarc.bellclaims.domain.values.Position3D
import java.util.UUID

interface VisualisationService {
    fun displayComplete(playerId: UUID, partitions: Set<Position2D>): Set<Position3D>
    fun displayPartitioned(playerId: UUID, partitions: Set<Position2D>): Set<Position3D>
    fun displayOthers(playerId: UUID, partitions: Set<Position2D>): Set<Position3D>
    fun clear(playerId: UUID, partitions: Set<Position2D>): Set<Position3D>
    fun clearAll(playerId: UUID): Set<Position2D>
}