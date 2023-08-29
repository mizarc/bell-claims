package dev.mizarc.bellclaims.api

import dev.mizarc.bellclaims.api.enums.PartitionCreationResult
import dev.mizarc.bellclaims.api.enums.PartitionDestroyResult
import dev.mizarc.bellclaims.api.enums.PartitionResizeResult
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.partitions.Area
import dev.mizarc.bellclaims.domain.partitions.Partition
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import java.util.UUID

/**
 * A service that handles the querying, creation, modification, and deletion of partitions.
 */
interface PartitionService {

    /**
     * Checks to see if a new partition placed into the world would be valid based on overlap and minimum distance.
     *
     * @param area The area to query.
     * @param world The world the partition will be added to.
     * @return True if the queried area is valid.
     */
    fun isAreaValid(area: Area, world: World): Boolean

    /**
     * Checks to see if a new partition to be attached to an existing claim would be valid based on overlap and minimum
     * distance conditions.
     *
     * @param area The area to query.
     * @param claim The claim that the partition would be attached to.
     * @return True if the queried area is valid.
     */
    fun isAreaValid(area: Area, claim: Claim): Boolean

    /**
     * Checks to see if the target partition can be removed without breaking the claim structure.
     * @param partition The partition to remove.
     * @return True if the partition can be removed.
     */
    fun isRemoveAllowed(partition: Partition): Boolean

    /**
     * Gets a partition by its unique id.
     *
     * @param uuid The UUID of the partition.
     * @return The found partition or null if not found.
     */
    fun getById(uuid: UUID): Partition?

    /**
     * Gets the partition that exists at the location in the world.
     *
     * @param location The Location to query.
     * @return The found partition or null if not found.
     */
    fun getByLocation(location: Location): Partition?

    /**
     * Gets all partitions that exist in a chunk.
     *
     * @param chunk The Chunk to query.
     * @return A set of partitions that exist in the chunk.
     */
    fun getByChunk(chunk: Chunk): Set<Partition>

    /**
     * Gets all partitions that are linked to a claim.
     *
     * @param claim The claim to get the partitions from.
     * @return A set of partitions that are linked to the claim.
     */
    fun getByClaim(claim: Claim): Set<Partition>

    /**
     * Gets the partition that the claim bell exists in.
     *
     * @param claim The claim to get the partition of.
     * @return The primary partition.
     */
    fun getPrimary(claim: Claim): Partition?

    /**
     * Adds a new partition to the claim using an area.
     * @param area The area to compose the partition out of.
     * @param claim The claim to add the partition to.
     * @return An enum detailing the result of the append action.
     */
    fun append(area: Area, claim: Claim): PartitionCreationResult

    /**
     * Resizes an existing partition to a new size.
     * @param partition The partition to resize.
     * @param area The new area to set the partition size to.
     * @return An enum detailing the result of the resize action.
     */
    fun resize(partition: Partition, area: Area): PartitionResizeResult

    /**
     * Deletes an existing partition.
     * @param partition The partition to delete
     * @return An enum detailing the result of the delete action.
     */
    fun delete(partition: Partition): PartitionDestroyResult
}