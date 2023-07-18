package xyz.mizarc.solidclaims

import net.kyori.adventure.text.BlockNBTComponent.Pos
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.claims.*
import xyz.mizarc.solidclaims.partitions.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * A service class that allows for the querying and modification of partitions in the world.
 */
class PartitionService(private val claimService: ClaimService, private val partitionRepo: PartitionRepository) {

    /**
     * An enum representing the result of a partition creation operation.
     */
    enum class PartitionCreationResult {
        Overlap,
        TooSmall,
        InsufficientBlocks,
        NotConnected,
        Successful
    }

    /**
     * An enum representing the result of a partition resizing operation.
     */
    enum class PartitionResizeResult {
        Overlap,
        TooSmall,
        InsufficientBlocks,
        DisconnectedPartition,
        ExposedClaimHub,
        Successful
    }

    /**
     * Checks if the target location overlaps an existing partition.
     * @param location The target location.
     * @return True if the location exists within a partition, false otherwise.
     */
    fun isLocationOverlap(location: Location): Boolean {
        val chunk = Position2D(location).toChunk()
        val partitionsInChunk = filterByWorld(location.world!!.uid, partitionRepo.getByChunk(chunk))
        for (partition in partitionsInChunk) {
            if (partition.isPositionInPartition(Position2D(location))) {
                return true
            }
        }
        return false
    }

    /**
     * Checks if a partition being put into the world would overlap any existing partition.
     * @param partition The new partition.
     * @param worldId The world to put the partition in.
     * @return True if the partition area would result in an overlap.
     */
    fun isPartitionOverlap(partition: Partition, worldId: UUID): Boolean {
        val chunks = partition.area.getChunks()
        val existingPartitions: MutableSet<Partition> = mutableSetOf()
        for (chunk in chunks) {
            existingPartitions.addAll(filterByWorld(worldId, getByChunk(worldId, chunk)))
        }

        existingPartitions.removeAll { it.id == partition.id }
        for (existingPartition in existingPartitions) {
            if (partition.isAreaOverlap(existingPartition.area)) {
                return true
            }
        }

        return false
    }

    /**
     * Gets the partition at the current location.
     * @param location The target location.
     * @return The partition that exists at the target location.
     */
    fun getByLocation(location: Location): Partition? {
        val foundPartitions = partitionRepo.getByPosition(Position2D(location))
        for (partition in foundPartitions) {
            val foundClaim = claimService.getById(partition.claimId) ?: continue
            if (foundClaim.worldId == location.world!!.uid) {
                return partition
            }
        }
        return null
    }

    /**
     * Gets all partitions in the target chunk.
     * @param chunk The target chunk.
     * @return A set of partitions that exist within the target chunk.
     */
    fun getByChunk(chunk: Chunk): Set<Partition> {
        return filterByWorld(chunk.world.uid, partitionRepo.getByChunk(Position2D(chunk.x, chunk.z)))
    }

    /**
     * Gets all partitions in the target chunk.
     * @param worldId The world the chunk is in.
     * @param position The coordinates of the chunk.
     * @return A set of partitions that exist within the target chunk.
     */
    fun getByChunk(worldId: UUID, position: Position2D): Set<Partition> {
        return filterByWorld(worldId, partitionRepo.getByChunk(position))
    }

    /**
     * Gets the partition the player is standing in.
     */
    fun getByPlayerPosition(player: Player): Partition? {
        val claimPartition = getByLocation(player.location)
        if (claimPartition == null) {
            player.sendMessage("§cThere is no claim partition at your current location.")
            return null
        }
        return claimPartition
    }

    /**
     * Gets all partitions that are adjacent and connected to the target partition.
     * @param partition The target partition.
     * @return The partitions in the world that are physically touching the target partition.
     */
    fun getAdjacent(partition: Partition): ArrayList<Partition> {
        // Find all partitions within the chunks of the partition
        val claim = claimService.getById(partition.claimId) ?: return ArrayList()
        val chunkPartitions = ArrayList<Partition>()
        val chunks = partition.getChunks()
        for (chunk in chunks) {
            chunkPartitions.addAll(getByChunk(claim.worldId, chunk))
        }

        // Find which of the partitions in the chunks are adjacent
        val adjacentPartitions = ArrayList<Partition>()
        for (chunkPartition in chunkPartitions) {
            if (chunkPartition.isPartitionAdjacent(partition)) {
                adjacentPartitions.add(chunkPartition)
            }
        }
        return adjacentPartitions
    }

    /**
     * Gets the partitions that are adjacent to the target location.
     *
     */
    fun getAdjacent(location: Location): Set<Partition> {
        val positions = getSurroundingPositions(Position2D(location), 1)

        val partitions: MutableSet<Partition> = mutableSetOf()
        for (position in positions) {
            val foundPartitions = partitionRepo.getByPosition(position)
            if (foundPartitions.isNotEmpty()) {
                val partition = filterByWorld(location.world.uid, foundPartitions.toSet()).first()
                partitions.add(partition)
            }
        }
        return partitions
    }

    /**
     * Gets the partition that the claim bell is located in.
     * @param claim The target claim.
     * @return The partition that the target claim's claim bell is physically located in.
     */
    fun getPrimaryPartition(claim: Claim): Partition {
        val claimPartitions = partitionRepo.getByClaim(claim)
        return partitionRepo.getByPosition(Position2D(claim.position)).intersect(claimPartitions.toSet()).first()
    }

    /**
     * Gets all partitions
     */
    fun getLinked(partition: Partition, testPartitions: ArrayList<Partition>): ArrayList<Partition> {
        val linkedPartitions = ArrayList<Partition>()
        for (existingPartition in testPartitions) {
            if (existingPartition.isPartitionLinked(partition) && existingPartition.claimId == partition.claimId) {
                linkedPartitions.add(existingPartition)
            }
        }
        return linkedPartitions
    }

    /**
     * Adds a new partition to the world. If adjacent to an existing partition that the player owns, will link to it.
     * @param player The player that is performing the action, this is used to check for claim limits.
     * @param partition The partition to add.
     * @param worldId The world to add the partition to.
     */
    fun addPartition(player: OfflinePlayer, partition: Partition, worldId: UUID): PartitionCreationResult {
        // Set second location & Check if it overlaps an existing claim
        if (isPartitionOverlap(partition, worldId)) {
            return PartitionCreationResult.Overlap
        }

        // Check if claim meets minimum size
        if (partition.area.getXLength() < 5 || partition.area.getZLength() < 5) {
            return PartitionCreationResult.TooSmall
        }

        // Check if selection is greater than the player's remaining claim blocks
        val remainingClaimBlockCount = claimService.getRemainingClaimBlockCount(player)!!
        if (partition.area.getBlockCount() > remainingClaimBlockCount) {
            return PartitionCreationResult.InsufficientBlocks
        }

        // Append partition to existing claim if adjacent to claim owned by player
        val adjacentPartitions = getAdjacent(partition)
        for (adjacentPartition in adjacentPartitions) {
            val claim = claimService.getById(adjacentPartitions[0].claimId) ?: continue
            if (player.uniqueId == claim.owner.uniqueId) {
                partition.claimId = claim.id
                partitionRepo.add(partition)
                return PartitionCreationResult.Successful
            }
        }

        return PartitionCreationResult.NotConnected
    }

    /**
     * Resizes an existing partition. Ensures that the change doesn't result in any linked partitions being detached
     * from the main partition in the claim.
     * @param partition The partition to resize.
     * @param newArea The new area the partition will occupy.
     * @return The result of the action depending on whether the resizing was successful or what reason made it
     * unsuccessful
     */
    fun resizePartition(player: OfflinePlayer, partition: Partition): PartitionResizeResult {
        // Check if selection overlaps an existing claim
        val claim = claimService.getById(partition.claimId) ?: return PartitionResizeResult.Overlap
        if (isPartitionOverlap(partition, claim.worldId)) {
            return PartitionResizeResult.Overlap
        }

        if (partition.id == getPrimaryPartition(claim).id && !partition.area.isPositionInArea(claim.position)) {
            return PartitionResizeResult.ExposedClaimHub
        }

        // Check if claim meets minimum size
        if (partition.area.getXLength() < 5 || partition.area.getZLength() < 5) {
            return PartitionResizeResult.TooSmall
        }

        // Check if claim takes too much space
        val remainingClaimBlockCount = claimService.getRemainingClaimBlockCount(player)!!
        if (claimService.getUsedClaimBlockCount(player) + (partition.area.getBlockCount() - partition.area.getBlockCount())
                > remainingClaimBlockCount) {
            return PartitionResizeResult.InsufficientBlocks
        }

        // Check if claim resize would result a partition being disconnected from the main
        if (isResizeResultInAnyDisconnected(partition)) {
            return PartitionResizeResult.DisconnectedPartition
        }

        partitionRepo.update(partition)
        return PartitionResizeResult.Successful
    }

    fun removePartition(partition: Partition): Boolean {
        if (isRemoveResultInAnyDisconnected(partition)) {
            return false
        }
        partitionRepo.remove(partition)
        return true
    }


    private fun appendPartitionToClaim(player: Player, partition: Partition, claim: Claim) {
        partitionRepo.add(partition)
        val name = if (claim.name.isEmpty()) claim.name else claim.id.toString().substring(0, 7)
        player.sendMessage("§aNew claim partition has been added to §6$name.")
    }

    private fun isResizeResultInAnyDisconnected(partition: Partition): Boolean {
        val claim = claimService.getById(partition.claimId) ?: return false
        val claimPartitions = partitionRepo.getByClaim(claim)
        val mainPartition = partitionRepo.getByPosition(Position2D(claim.position))
            .intersect(claimPartitions.toSet()).first()
        claimPartitions.removeAll { it.id == partition.id }
        claimPartitions.add(partition)
        for (claimPartition in claimPartitions) {
            if (partition.id == mainPartition.id) {
                continue
            }
            if (!isPartitionDisconnected(partition, claimPartitions)) {
                return true
            }
        }
        return false
    }

    private fun isRemoveResultInAnyDisconnected(partition: Partition): Boolean {
        val claim = claimService.getById(partition.claimId) ?: return false
        val claimPartitions = partitionRepo.getByClaim(claim)
        val mainPartition = partitionRepo.getByPosition(Position2D(claim.position))
            .intersect(claimPartitions.toSet()).first()
        claimPartitions.remove(partition)
        for (claimPartition in claimPartitions) {
            if (partition.id == mainPartition.id) {
                continue
            }
            if (!isPartitionDisconnected(partition, claimPartitions)) {
                return true
            }
        }
        return false
    }

    private fun isPartitionDisconnected(partition: Partition, testPartitions: ArrayList<Partition>): Boolean {
        val claim = claimService.getById(partition.claimId) ?: return false
        val claimPartitions = partitionRepo.getByClaim(claim)
        val mainPartition = partitionRepo.getByPosition(Position2D(claim.position))
            .intersect(claimPartitions.toSet()).first()
        val traversedPartitions = ArrayList<Partition>()
        val partitionQueries = ArrayList<Partition>()
        partitionQueries.add(partition)
        while(partitionQueries.isNotEmpty()) {
            val partitionsToAdd = ArrayList<Partition>()
            val partitionsToRemove = ArrayList<Partition>()
            for (partitionQuery in partitionQueries) {
                val adjacentPartitions = getLinked(partition, testPartitions)
                for (adjacentPartition in adjacentPartitions) {
                    if (adjacentPartition.id == mainPartition.id) {
                        return true
                    }
                    if (adjacentPartition in traversedPartitions) {
                        continue
                    }
                    partitionsToAdd.add(adjacentPartition)
                }
                partitionsToRemove.add(partitionQuery)
                traversedPartitions.add(partitionQuery)
            }
            partitionQueries.removeAll(partitionsToRemove.toSet())
            partitionQueries.addAll(partitionsToAdd)
            partitionsToAdd.clear()
        }
        return false
    }

    private fun filterByWorld(worldId: UUID, inputPartitions: Set<Partition>): Set<Partition> {
        val outputPartitions = mutableSetOf<Partition>()
        for (partition in inputPartitions) {
            val claimPartition = claimService.getById(partition.claimId) ?: continue
            if (claimPartition.worldId == worldId) {
                outputPartitions.add(partition)
            }
        }
        return outputPartitions
    }

    fun getSurroundingPositions(position: Position2D, radius: Int): List<Position2D> {
        val positions = mutableListOf<Position2D>()

        for (i in -1 * radius..1 * radius) {
            for (j in -1 * radius..1 * radius) {
                positions.add(Position2D(position.x + i, position.z + j))
            }
        }
        return positions
    }
}