package dev.mizarc.bellclaims.api

import dev.mizarc.bellclaims.api.enums.PartitionCreationResult
import dev.mizarc.bellclaims.api.enums.PartitionDestroyResult
import dev.mizarc.bellclaims.api.enums.PartitionResizeResult
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.partitions.Area
import dev.mizarc.bellclaims.domain.partitions.Partition
import org.bukkit.Location
import java.util.UUID

/**
 * A service that handles the querying, creation, modification, and deletion of partitions.
 */
interface PartitionService {
    /**
     * Checks to see if an area placed in the world is valid based on overlap and minimum distance conditions.
     *
     * @param area The area to query.
     * @param claim The claim that the partition would be attached to.
     * @return True if the queried area is valid.
     */
    fun isAreaValid(area: Area, claim: Claim): Boolean

    /**
     * Gets a partition by its unique id.
     *
     * @param uuid The UUID of the partition.
     * @return The found partition or null if not found.
     */
    fun getPartitionById(uuid: UUID): Partition?

    /**
     * Gets the partition that exists at the location in the world.
     *
     * @param location The Location to query.
     * @return The found partition or null if not found.
     */
    fun getPartitionByLocation(location: Location): Partition?

    /**
     * Gets all partitions that are linked to a claim.
     *
     * @param claim The claim to get the partitions from.
     * @return A set of partitions that are linked to the claim.
     */
    fun getPartitionsByClaim(claim: Claim): Set<Partition>

    /**
     * Adds a new partition to the claim using an area.
     * @param area The area to compose the partition out of.
     * @param claim The claim to add the partition to.
     * @return An enum detailing the result of the append action.
     */
    fun appendPartition(area: Area, claim: Claim): PartitionCreationResult

    /**
     * Resizes an existing partition to a new size.
     * @param partition The partition to resize.
     * @param area The new area to set the partition size to.
     * @return An enum detailing the result of the resize action.
     */
    fun resizePartition(partition: Partition, area: Area): PartitionResizeResult

    /**
     * Deletes an existing partition.
     * @param partition The partition to delete
     * @return An enum detailing the result of the delete action.
     */
    fun deletePartition(partition: Partition): PartitionDestroyResult
}