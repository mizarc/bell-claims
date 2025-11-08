package dev.mizarc.bellclaims.application.actions.player.visualisation

import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PartitionRepository
import dev.mizarc.bellclaims.application.persistence.PlayerStateRepository
import dev.mizarc.bellclaims.application.services.VisualisationService
import dev.mizarc.bellclaims.config.MainConfig
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.entities.PlayerState
import dev.mizarc.bellclaims.domain.values.Position2D
import dev.mizarc.bellclaims.domain.values.Position3D
import java.time.Duration
import java.time.Instant
import java.util.UUID
import kotlin.collections.set
import kotlin.math.floor
import org.bukkit.Bukkit

class DisplayVisualisation(private val playerStateRepository: PlayerStateRepository,
                           private val claimRepository: ClaimRepository,
                           private val partitionRepository: PartitionRepository,
                           private val visualisationService: VisualisationService,
                           private val clearVisualisation: ClearVisualisation,
                           private val config: MainConfig) {
    /**
     * Display claim visualisation to target player
     */
    fun execute(playerId: UUID, playerPosition: Position3D): MutableMap<UUID, Set<Position3D>> {
        var playerState = playerStateRepository.get(playerId)
        if (playerState == null) {
            playerState = PlayerState(playerId)
            playerStateRepository.add(playerState)
        }

        // Fetch the player's current world so we only visualise claims in that world
        val player = Bukkit.getPlayer(playerId) ?: return mutableMapOf()
        val worldId = player.world.uid

        // If there is a scheduled hide task, cancel it immediately — the player is re-displaying.
        playerState.scheduledVisualiserHide?.cancel()
        playerState.scheduledVisualiserHide = null

        // Check if allowed to refresh visualisation based on the last visualisation time
        // If not reached cooldown time, refresh the last visualisation time to set the new cooldown
        val refreshPeriodInMillis = (config.visualiserRefreshPeriod * 1000).toLong()
        val lastVisualisation = playerState.lastVisualisationTime
        if (lastVisualisation != null
                && lastVisualisation.plus(Duration.ofMillis(refreshPeriodInMillis)).isAfter(Instant.now())) {
            playerState.lastVisualisationTime = Instant.now()
            playerStateRepository.update(playerState)
            return mutableMapOf()
        }

        // Clear the active visualisation
        clearVisualisation.execute(playerId)

        // Change visualiser depending on view mode
        val borders: MutableMap<UUID, Set<Position3D>> = mutableMapOf()
        if (playerState.claimToolMode == 1) {
            borders.putAll(displayPartitioned(playerId, playerState, playerPosition, worldId))
        }
        else {
            borders.putAll(displayComplete(playerId, playerState, playerPosition, worldId))
            playerState.visualisedClaims = borders
        }

        // Set visualisation in the player state
        playerState.isVisualisingClaims = true
        playerState.lastVisualisationTime = Instant.now()
        playerStateRepository.update(playerState)
        return borders
    }

    /**
     * Visualise all of a player's claims with only outer borders.
     */
    private fun displayComplete(playerId: UUID, playerState: PlayerState, playerPosition: Position3D, worldId: UUID): Map<UUID, Set<Position3D>> {
        val chunkPosition = Position2D(floor(playerPosition.x / 16.0).toInt(), floor(playerPosition.z / 16.0).toInt())
        val chunks = getSurroundingChunks(chunkPosition, 16) // View distance fixed for now
        val partitions = chunks.flatMap { partitionRepository.getByChunk(it) }
            .filter { claimRepository.getById(it.claimId)?.worldId == worldId }
            .toSet()
        if (partitions.isEmpty()) return mutableMapOf()

        // Mapping claim ids to the positions assigned to that claim
        val visualised: MutableMap<UUID, Set<Position3D>> = mutableMapOf()
        for (partition in partitions) {
            if (visualised.containsKey(partition.claimId)) continue
            val claim = claimRepository.getById(partition.claimId) ?: continue

            // Handle claim not owned by this player
            if (claim.playerId != playerId) {
                visualised[claim.id] = handleNonOwnedClaimDisplay(playerId, claim).filter {
                    it != playerState.selectedBlock }
                    .toSet()
            } else {
                // Get all partitions linked to the found claim
                val partitions = partitionRepository.getByClaim(claim.id)
                val areas = partitions.map { it.area }.toMutableSet()

                // Visualise the entire claim border and map it
                visualised[claim.id] = visualisationService.displayComplete(playerId, areas,
                    "LIGHT_BLUE_GLAZED_TERRACOTTA", "LIGHT_BLUE_CARPET", "BLUE_GLAZED_TERRACOTTA", "BLUE_CARPET").filter {
                        it != playerState.selectedBlock }
                    .toSet()

            }
        }
        return visualised
    }

    /**
     * Visualise a player's claims with individual partitions shown.
     */
    private fun displayPartitioned(playerId: UUID, playerState: PlayerState, playerPosition: Position3D, worldId: UUID): Map<UUID, Set<Position3D>> {
        val chunkPosition = Position2D(floor(playerPosition.x / 16.0).toInt(), floor(playerPosition.z / 16.0).toInt())
        val chunks = getSurroundingChunks(chunkPosition, 16) // View distance fixed for now
        val partitions = chunks.flatMap { partitionRepository.getByChunk(it) }
            .filter { claimRepository.getById(it.claimId)?.worldId == worldId }
            .toSet()
        if (partitions.isEmpty()) return mutableMapOf()

        // Mapping claim ids to the positions assigned to that claim
        val visualised: MutableMap<UUID, MutableSet<Position3D>> = mutableMapOf()
        for (partition in partitions) {
            val claim = claimRepository.getById(partition.claimId) ?: continue

            // Handle claim not owned by this player
            if (claim.playerId != playerId) {
                val positions = handleNonOwnedClaimDisplay(playerId, claim).toMutableSet()
                visualised[claim.id] = positions
                playerState.visualisedClaims[claim.id] = positions
                continue
            }

            // Visualise the partition and add it to the map assigned to the partition's claim
            val newPositions = if (partition.area.isPositionInArea(claim.position)) {
                // Main partition
                visualisationService.displayPartitioned(playerId, setOf(partition.area),
                    "CYAN_GLAZED_TERRACOTTA", "CYAN_CARPET",
                    "BLUE_GLAZED_TERRACOTTA", "BLUE_CARPET").filter { it != playerState.selectedBlock }
                    .toSet()
            } else {
                // Attached partitions
                visualisationService.displayPartitioned(playerId, setOf(partition.area),
                    "LIGHT_GRAY_GLAZED_TERRACOTTA", "LIGHT_GRAY_CARPET",
                    "BLUE_GLAZED_TERRACOTTA", "BLUE_CARPET").filter { it != playerState.selectedBlock }
                    .toSet()
            }

            visualised.computeIfAbsent(claim.id) { mutableSetOf() }.addAll(newPositions)
            playerState.visualisedPartitions.computeIfAbsent(claim.id) { mutableMapOf() }[partition.id] = newPositions
        }
        return visualised
    }

    private fun handleNonOwnedClaimDisplay(playerId: UUID, claim: Claim): Set<Position3D> {
        // Get all partitions linked to the found claim
        val partitions = partitionRepository.getByClaim(claim.id)
        val areas = partitions.map { it.area }.toMutableSet()
        return visualisationService.displayComplete(playerId, areas, "RED_GLAZED_TERRACOTTA", "RED_CARPET", "BLACK_GLAZED_TERRACOTTA", "BLACK_CARPET")
    }
    
    /**
     * Get a square of chunks centering on [chunkPosition] with a size of [radius]
     */
    private fun getSurroundingChunks(chunkPosition: Position2D, radius: Int): Set<Position2D> {
        val sideLength = (radius * 2) + 1 // Make it always odd (e.g. radius of 2 results in 5x5 square)
        val chunks: MutableSet<Position2D> = mutableSetOf()

        for (x in 0 until sideLength) {
            for (z in 0 until sideLength) {
                chunks.add(Position2D(chunkPosition.x + x - radius, chunkPosition.z + z - radius))
            }
        }

        return chunks
    }
}