package xyz.mizarc.solidclaims

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.claims.Claim
import xyz.mizarc.solidclaims.claims.ClaimRepository
import xyz.mizarc.solidclaims.claims.ClaimRuleRepository
import xyz.mizarc.solidclaims.listeners.ClaimRule
import xyz.mizarc.solidclaims.partitions.*
import xyz.mizarc.solidclaims.players.PlayerStateRepository
import java.util.*
import kotlin.collections.ArrayList

class ClaimQuery(val claims: ClaimRepository, val partitions: PartitionRepository, val claimRuleRepository: ClaimRuleRepository, val playerStates: PlayerStateRepository) {

    enum class PartitionCreationResult {
        Overlap,
        TooSmall,
        InsufficientClaims,
        InsufficientBlocks,
        NotConnected,
        Successful
    }

    enum class PartitionResizeResult {
        Overlap,
        TooSmall,
        InsufficientBlocks,
        DisconnectedPartition,
        Successful
    }

    fun isLocationOverlap(location: Location): Boolean {
        val chunk = Position2D(location).toChunk()
        val partitionsInChunk = filterByWorld(location.world!!.uid, partitions.getByChunk(chunk))
        for (partition in partitionsInChunk) {
            Bukkit.getLogger().info("$partition")
            if (partition.isPositionInPartition(Position2D(location))) {
                return true
            }
        }
        return false
    }

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

    fun getBlockCount(claim: Claim): Int {
        val claimPartitions = partitions.getByClaim(claim)
        var count = 0
        for (partition in claimPartitions) {
            count += partition.area.getBlockCount()
        }
        return count
    }

    fun getByLocation(location: Location): Partition? {
        val foundPartitions = partitions.getByPosition(Position2D(location))
        for (partition in foundPartitions) {
            val foundClaim = claims.getById(partition.claimId) ?: continue
            if (foundClaim.worldId == location.world!!.uid) {
                return partition
            }
        }
        return null
    }

    fun getByChunk(worldPosition: WorldPosition): ArrayList<Partition> {
        return filterByWorld(worldPosition.worldId, partitions.getByChunk(worldPosition.toChunk()))
    }

    fun getByPlayer(player: Player): Partition? {
        val claimPartition = getByLocation(player.location)
        if (claimPartition == null) {
            player.sendMessage("§cThere is no claim partition at your current location.")
            return null
        }
        return claimPartition
    }

    fun getAdjacent(partition: Partition): ArrayList<Partition> {
        // Find all partitions within the chunks of the partition
        val claim = claims.getById(partition.claimId) ?: return ArrayList()
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

    fun getMainPartition(claim: Claim): Partition {
        val claimPartitions = partitions.getByClaim(claim)
        return partitions.getByPosition(Position2D(claim.position)).intersect(claimPartitions.toSet()).first()
    }

    fun getLinked(partition: Partition, testPartitions: ArrayList<Partition>): ArrayList<Partition> {
        val linkedPartitions = ArrayList<Partition>()
        for (existingPartition in testPartitions) {
            if (existingPartition.isPartitionLinked(partition) && existingPartition.claimId == partition.claimId) {
                linkedPartitions.add(existingPartition)
            }
        }
        return linkedPartitions
    }

    fun getUsedClaimCount(player: OfflinePlayer): Int {
        var count = 0
        val playerClaims = claims.getByPlayer(player)
        for (claim in playerClaims) {
            count += 1
        }
        return count
    }

    fun getUsedClaimBlockCount(player: OfflinePlayer): Int {
        var count = 0
        val playerClaims = claims.getByPlayer(player)
        for (claim in playerClaims) {
            count += getBlockCount(claim)
        }
        return count
    }

    fun getRemainingClaimCount(player: OfflinePlayer): Int? {
        val playerState = playerStates.get(player) ?: return null
        return playerState.getClaimLimit() - getUsedClaimCount(player)
    }

    fun getRemainingClaimBlockCount(player: OfflinePlayer): Int? {
        val playerState = playerStates.get(player) ?: return null
        return playerState.getClaimBlockLimit() - getUsedClaimBlockCount(player)
    }

    fun getClaimRules(claim: Claim): MutableSet<ClaimRule>? {
        return claimRuleRepository.getByClaim(claim)
    }

    /**
     * Adds a new partition to the world. If adjacent to an existing partition that the player owns, will link to it.
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
        val remainingClaimBlockCount = getRemainingClaimBlockCount(player)!!
        if (partition.area.getBlockCount() > remainingClaimBlockCount) {
            return PartitionCreationResult.InsufficientBlocks
        }

        // Append partition to existing claim if adjacent to claim owned by player
        val adjacentPartitions = getAdjacent(partition)
        for (adjacentPartition in adjacentPartitions) {
            val claim = claims.getById(adjacentPartitions[0].claimId) ?: continue
            if (player.uniqueId == claim.owner.uniqueId) {
                partition.claimId = claim.id
                partitions.add(partition)
                return PartitionCreationResult.Successful
            }
        }

        return PartitionCreationResult.NotConnected
    }

    /**
     * Resizes an existing partition. Ensures that the change doesn't result in any linked partitions being detached
     * from the main partition in the claim.
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
        val remainingClaimBlockCount = getRemainingClaimBlockCount(player)!!
        if (getUsedClaimBlockCount(player) + (newArea.getBlockCount() - partition.area.getBlockCount())
                > remainingClaimBlockCount) {
            return PartitionResizeResult.InsufficientBlocks
        }

        // Check if claim resize would result a partition being disconnected from the main
        if (isResizeResultInAnyDisconnected(partition, newArea)) {
            return PartitionResizeResult.DisconnectedPartition
        }

        partitions.update(Partition(partition.id, partition.claimId, newArea))
        return PartitionResizeResult.Successful
    }

    fun removePartition(partition: Partition): Boolean {
        if (isRemoveResultInAnyDisconnected(partition)) {
            return false
        }
        partitions.remove(partition)
        return true
    }

    private fun appendPartitionToClaim(player: Player, partition: Partition, claim: Claim) {
        partitions.add(partition)
        val name = if (claim.name.isEmpty()) claim.name else claim.id.toString().substring(0, 7)
        player.sendMessage("§aNew claim partition has been added to §6$name.")
    }

    private fun isResizeResultInAnyDisconnected(partition: Partition, newArea: WorldArea): Boolean {
        val claim = claims.getById(partition.claimId) ?: return false
        val claimPartitions = partitions.getByClaim(claim)
        val mainPartition = partitions.getByPosition(Position2D(claim.position))
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
        val claim = claims.getById(partition.claimId) ?: return false
        val claimPartitions = partitions.getByClaim(claim)
        val mainPartition = partitions.getByPosition(Position2D(claim.position))
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
        val claim = claims.getById(partition.claimId) ?: return false
        val claimPartitions = partitions.getByClaim(claim)
        val mainPartition = partitions.getByPosition(Position2D(claim.position))
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

    private fun filterByWorld(worldId: UUID, inputPartitions: ArrayList<Partition>): ArrayList<Partition> {
        val outputPartitions = arrayListOf<Partition>()
        for (partition in inputPartitions) {
            val claimPartition = claims.getById(partition.claimId) ?: continue
            if (claimPartition.worldId == worldId) {
                outputPartitions.add(partition)
            }
        }
        return outputPartitions
    }
}