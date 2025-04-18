package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.application.actions.claim.metadata.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.claim.partition.GetClaimPartitions
import dev.mizarc.bellclaims.application.actions.player.visualisation.IsPlayerVisualising
import dev.mizarc.bellclaims.application.actions.player.visualisation.RefreshVisualisation
import dev.mizarc.bellclaims.application.events.PartitionModificationEvent
import dev.mizarc.bellclaims.application.results.player.visualisation.IsPlayerVisualisingResult
import dev.mizarc.bellclaims.domain.entities.Claim
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PartitionUpdateListener: Listener, KoinComponent {
    private val getClaimDetails: GetClaimDetails by inject()
    private val getClaimPartitions: GetClaimPartitions by inject()
    private val isPlayerVisualising: IsPlayerVisualising by inject()
    private val refreshVisualisation: RefreshVisualisation by inject()

    @EventHandler
    fun onPartitionUpdate(event: PartitionModificationEvent) {
        val claim = getClaimDetails.execute(event.partition.claimId) ?: return
        val nearbyPlayers = getNearbyPlayers(claim)
        for (player in nearbyPlayers) refreshVisualisation.execute(player.uniqueId, claim.id, event.partition.id)
    }

    /**
     * Get all players who are in visualisation range of a claim.
     */
    private fun getNearbyPlayers(claim: Claim): Array<Player> {
        val players: ArrayList<Player> = ArrayList()

        // Get list of chunks that the claim occupies
        val startingChunks: MutableSet<Chunk> = mutableSetOf()
        val partitions = getClaimPartitions.execute(claim.id)
        val world = Bukkit.getWorld(claim.worldId) ?: return arrayOf()
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
            if (finalChunks.contains(player.chunk)) {
                val result = isPlayerVisualising.execute(player.uniqueId)
                when (result) {
                    is IsPlayerVisualisingResult.Success -> players.add(player)
                    else -> {}
                }
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