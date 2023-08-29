package dev.mizarc.bellclaims.api

import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.partitions.Partition
import dev.mizarc.bellclaims.domain.partitions.Position2D
import dev.mizarc.bellclaims.domain.partitions.Position3D

interface VisualisationService {
    fun getOuterBorders(claim: Claim): Set<Position2D>
    fun getPartitionedBorders(claim: Claim): Map<Partition, Set<Position2D>>
    fun get3DOuterBorders(claim: Claim): Set<Position3D>
    fun get3DPartitionedBorders(claim: Claim): Map<Partition, Set<Position3D>>
}