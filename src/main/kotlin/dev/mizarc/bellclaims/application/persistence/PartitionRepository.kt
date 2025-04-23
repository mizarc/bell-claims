package dev.mizarc.bellclaims.application.persistence

import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.entities.Partition
import dev.mizarc.bellclaims.domain.values.Position
import java.util.*

/**
 * A repository that handles the persistence of Partitions.
 */
interface PartitionRepository {
    /**
     * Gets all partitions that exist
     *
     * @return The set of partitions that exist.
     */
    fun getAll(): Set<Partition>

    /**
     * Gets a partition by its id.
     *
     * @param id The unique id of the partition.
     * @return The found partition, or null if not found.
     */
    fun getById(id: UUID): Partition?

    /**
     * Gets all partitions that are linked to a given claim.
     *
     * @param claimId The id of the claim that owns the partitions.
     * @return The set of partitions linked to this claim.
     */
    fun getByClaim(claimId: UUID): Set<Partition>

    /**
     * Gets the partitions that exist within a given position.
     *
     * @param position The position to query.
     * @return The set of partitions that exist at that position.
     */
    fun getByPosition(position: Position): Set<Partition>

    /**
     * Gets the partitions that exist within a given chunk
     * @param position The chunk position to query.
     * @return The set of partitions that existing at that chunk.
     */
    fun getByChunk(position: Position): Set<Partition>

    /**
     * Adds a new partition.
     *
     * @param partition The partition to add.
     */
    fun add(partition: Partition): Boolean

    /**
     * Updates the data of an existing partition
     *
     * @param partition The partition to update.
     */
    fun update(partition: Partition): Boolean

    /**
     * Removes an existing partition.
     *
     * @param partition The id of the partition to remove.
     */
    fun remove(partitionId: UUID): Boolean

    /**
     * Removes all partitions linked to a given claim.
     *
     * @param claimId The id of the claim to remove the partitions from.
     */
    fun removeByClaim(claimId: UUID): Boolean
}