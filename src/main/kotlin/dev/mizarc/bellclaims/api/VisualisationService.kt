package dev.mizarc.bellclaims.api

import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.partitions.Partition
import dev.mizarc.bellclaims.domain.partitions.Position2D
import dev.mizarc.bellclaims.domain.partitions.Position3D
import org.bukkit.Location

interface VisualisationService {
    fun getOuterBorders(claim: Claim): Set<Position2D>
    fun getPartitionedBorders(claim: Claim): Map<Partition, Set<Position2D>>
    fun getPartitionedCorners(claim: Claim): Map<Partition, Set<Position2D>>
    fun getMainPartitionBorders(claim: Claim): Set<Position2D>
    fun getMainPartitionCorners(claim: Claim): Set<Position2D>
    fun get3DOuterBorders(claim: Claim, renderLocation: Location): Set<Position3D>
    fun get3DPartitionedBorders(claim: Claim, renderLocation: Location): Map<Partition, Set<Position3D>>
    fun get3DPartitionedCorners(claim: Claim, renderLocation: Location): Map<Partition, Set<Position3D>>
    fun get3DMainPartitionBorders(claim: Claim, renderLocation: Location): Set<Position3D>
    fun get3DMainPartitionCorners(claim: Claim, renderLocation: Location): Set<Position3D>
}