package xyz.mizarc.solidclaims

import org.bukkit.Bukkit
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
        InsufficientClaims,
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
            Bukkit.getLogger().info("$partition")
            if (partition.isPositionInPartition(Position2D(location))) {
                return true
            }
        }
        return false
    }

    /**
     * Checks if the target area overlaps an existing partition.
     * @param worldArea The target area.
     * @return True if the area intersects a partition, false otherwise.
     */
    fun isAreaOverlap(worldArea: WorldArea): Boolean {
        val chunks = worldArea.getWorldChunks()
        val existingPartitions: MutableSet<Partition> = mutableSetOf()
        for (chunk in chunks) {
            existingPartitions.addAll(filterByWorld(worldArea.worldId, getByChunk(chunk)))
        }

        for (partition in existingPartitions) {
            if (partition.isAreaOverlap(worldArea)) {
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
     * @param worldPosition The target chunk position.
     * @return A set of partitions that exist within the target chunk.
     */
    fun getByChunk(worldPosition: WorldPosition): Set<Partition> {
        return filterByWorld(worldPosition.worldId, partitionRepo.getByChunk(worldPosition.toChunk()))
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
            chunkPartitions.addAll(getByChunk(WorldPosition(chunk, claim.worldId)))
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
        if (isAreaOverlap(WorldArea(partition.area, worldId))) {
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
    fun resizePartition(player: OfflinePlayer, partition: Partition, newArea: WorldArea): PartitionResizeResult {
        // Check if selection overlaps an existing claim
        if (isAreaOverlap(newArea)) {
            return PartitionResizeResult.Overlap
        }

        // Check if claim meets minimum size
        if (newArea.getXLength() < 5 || newArea.getZLength() < 5) {
            return PartitionResizeResult.TooSmall
        }

        // Check if claim takes too much space
        val remainingClaimBlockCount = claimService.getRemainingClaimBlockCount(player)!!
        if (claimService.getUsedClaimBlockCount(player) + (newArea.getBlockCount() - partition.area.getBlockCount())
                > remainingClaimBlockCount) {
            return PartitionResizeResult.InsufficientBlocks
        }

        // Check if claim resize would result a partition being disconnected from the main
        if (isResizeResultInAnyDisconnected(partition, newArea)) {
            return PartitionResizeResult.DisconnectedPartition
        }

        partitionRepo.update(Partition(partition.id, partition.claimId, newArea))
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

    private fun isResizeResultInAnyDisconnected(partition: Partition, newArea: WorldArea): Boolean {
        val claim = claimService.getById(partition.claimId) ?: return false
        val claimPartitions = partitionRepo.getByClaim(claim)
        val mainPartition = partitionRepo.getByPosition(Position2D(claim.position))
            .intersect(claimPartitions.toSet()).first()
        val newPartition = Partition(partition.id, partition.claimId, newArea)
        claimPartitions.remove(partition)
        claimPartitions.add(newPartition)
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
}