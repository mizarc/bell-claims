package dev.mizarc.bellclaims.application.services.old

import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.entities.Partition
import dev.mizarc.bellclaims.domain.values.Position2D
import dev.mizarc.bellclaims.domain.values.Position3D
import org.bukkit.Location

/**
 * A service that fetches the border positions that makes up claims.
 */
interface VisualisationService {
    /**
     * Gets the outer border positions of a given claim.
     *
     * @param claim The target claim.
     * @return The set of positions that makes up the outer borders of the claim.
     */
    fun getOuterBorders(claim: Claim): Set<Position2D>

    /**
     * Gets the border positions of each individual partition of a given claim.
     *
     * @param claim The target claim.
     * @return The map of partitions and their associated set of border positions.
     */
    fun getPartitionedBorders(claim: Claim): Map<Partition, Set<Position2D>>

    /**
     * Gets the corner positions of each individual partition of a given claim.
     *
     * @param claim The target claim.
     * @return The map of partitions and their associated set of corner positions.
     */
    fun getPartitionedCorners(claim: Claim): Map<Partition, Set<Position2D>>

    /**
     * Gets the border positions of the main partition of a given claim.
     *
     * @param claim The target claim.
     * @return The set of positions that makes up the main partition of the claim.
     */
    fun getMainPartitionBorders(claim: Claim): Set<Position2D>

    /**
     * Gets the corner positions of the main partition of a given claim.
     *
     * @param claim The target claim.
     * @return The set of border positions that makes up the main partition of the claim.
     */
    fun getMainPartitionCorners(claim: Claim): Set<Position2D>

    /**
     * Gets the 3D outer border positions, the first visible block above and below a target position, of a given claim.
     *
     * @param claim The target claim.
     * @return The set of 3D positions that makes up the outer borders of the claim.
     */
    fun get3DOuterBorders(claim: Claim, renderLocation: Location): Set<Position3D>

    /**
     * Gets the 3D border positions, the first visible block above and below a target position,
     * of each individual partition of a given claim.
     *
     * @param claim The target claim.
     * @return The map of partitions and their associated set of 3D border positions.
     */
    fun get3DPartitionedBorders(claim: Claim, renderLocation: Location): Map<Partition, Set<Position3D>>

    /**
     * Gets the 3D corner positions, the first visible block above and below a target position,
     * of each individual partition of a given claim.
     *
     * @param claim The target claim.
     * @return The map of partitions and their associated set of 3D corner positions.
     */
    fun get3DPartitionedCorners(claim: Claim, renderLocation: Location): Map<Partition, Set<Position3D>>

    /**
     * Gets the 3D border positions, the first visible block above and below a target position,
     * of the main partition of a given claim.
     *
     * @param claim The target claim.
     * @return The set of 3D positions that makes up the main partition of the claim.
     */
    fun get3DMainPartitionBorders(claim: Claim, renderLocation: Location): Set<Position3D>

    /**
     * Gets the 3D corner positions, the first visible block above and below a target position,
     * of the main partition of a given claim.
     *
     * @param claim The target claim.
     * @return The set of 3D border positions that makes up the main partition of the claim.
     */
    fun get3DMainPartitionCorners(claim: Claim, renderLocation: Location): Set<Position3D>
}