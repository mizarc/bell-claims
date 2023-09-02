package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.api.ClaimService
import dev.mizarc.bellclaims.api.PartitionService
import dev.mizarc.bellclaims.api.PlayerStateService
import dev.mizarc.bellclaims.api.events.PartitionModificationEvent
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.interaction.visualisation.Visualiser
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PartitionUpdateListener(private val claimService: ClaimService,
                              private val partitionService: PartitionService,
                              private val playerStateService: PlayerStateService,
                              private val visualiser: Visualiser): Listener {
    @EventHandler
    fun onPartitionUpdate(event: PartitionModificationEvent) {
        val claim = claimService.getById(event.partition.claimId) ?: return
        val nearbyPlayers = getNearbyPlayers(claim)
        for (player in nearbyPlayers) {
            val playerState = playerStateService.getByPlayer(player) ?: continue
            val visualisedClaim = playerState.visualisedBlockPositions[claim] ?: continue

            // Clear and redo visualisation for selected claim
            visualiser.revertVisualisedBlocks(player, visualisedClaim)
            playerState.visualisedBlockPositions.remove(claim)
            visualiser.show(player, claim)
        }
    }

    /**
     * Get all players who are in visualisation range of a claim.
     */
    private fun getNearbyPlayers(claim: Claim): Array<Player> {
        val players: ArrayList<Player> = ArrayList()

        // Get list of chunks that the claim occupies
        val startingChunks: MutableSet<Chunk> = mutableSetOf()
        val partitions = partitionService.getByClaim(claim)
        val world = claim.getWorld() ?: return arrayOf()
        for (partition in partitions) {
            startingChunks.addAll(partition.getChunks().map { world.getChunkAt(it.x, it.z) })
        }

        // Get surrounding chunks
        val finalChunks: MutableSet<Chunk> = mutableSetOf()
        for (chunk in startingChunks) {
            finalChunks.addAll(getSurroundingChunks(chunk, 1))
        }

        // Get players in chunks
        for (player in Bukkit.getOnlinePlayers()) {
            if (player.location.world != world) continue
            val playerState = playerStateService.getByPlayer(player) ?: continue
            if (finalChunks.contains(player.chunk) && playerState.isVisualisingClaims) {
                players.add(player)
            }
        }
        return players.toTypedArray()
    }

    private fun getSurroundingChunks(chunk: Chunk, radius: Int): Set<Chunk> {
        val sideLength = (radius * 2) + 1 // Make it always odd (e.g. radius of 2 results in 5x5 square)
        val chunks: MutableSet<Chunk> = mutableSetOf()

        for (x in 0 until sideLength) {
            for (z in 0 until sideLength) {
                chunks.add(chunk.world.getChunkAt(chunk.x + x - radius, chunk.z + z - radius))
            }
        }

        return chunks
    }
}