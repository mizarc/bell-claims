package dev.mizarc.bellclaims.domain.partitions

import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.partitions.Partition
import dev.mizarc.bellclaims.domain.partitions.Position
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
     * @param claim The claim that owns the partitions.
     * @return The set of partitions linked to this claim.
     */
    fun getByClaim(claim: Claim): Set<Partition>

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
    fun add(partition: Partition)

    /**
     * Updates the data of an existing partition
     *
     * @param partition The partition to update.
     */
    fun update(partition: Partition)

    /**
     * Removes an existing partition.
     *
     * @param partition The partition to remove.
     */
    fun remove(partition: Partition)

    /**
     * Removes all partitions linked to a given claim.
     *
     * @param claim The claim to remove the partitions from.
     */
    fun removeByClaim(claim: Claim)
}