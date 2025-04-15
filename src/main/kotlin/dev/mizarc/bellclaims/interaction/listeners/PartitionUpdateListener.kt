package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.application.actions.claim.metadata.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.claim.partition.GetClaimPartitions
import dev.mizarc.bellclaims.application.actions.player.visualisation.GetVisualisedClaimBlocks
import dev.mizarc.bellclaims.application.actions.player.visualisation.IsPlayerVisualising
import dev.mizarc.bellclaims.application.actions.player.visualisation.UnregisterVisualisation
import dev.mizarc.bellclaims.application.events.PartitionModificationEvent
import dev.mizarc.bellclaims.application.results.player.visualisation.GetVisualisedClaimBlocksResult
import dev.mizarc.bellclaims.application.results.player.visualisation.IsPlayerVisualisingResult
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.interaction.visualisation.Visualiser
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PartitionUpdateListener(private val visualiser: Visualiser): Listener, KoinComponent {
    private val getClaimDetails: GetClaimDetails by inject()
    private val getClaimPartitions: GetClaimPartitions by inject()
    private val isPlayerVisualising: IsPlayerVisualising by inject()
    private val unregisterVisualisation: UnregisterVisualisation by inject()
    private val getVisualisedClaimBlocks: GetVisualisedClaimBlocks by inject()

    @EventHandler
    fun onPartitionUpdate(event: PartitionModificationEvent) {
        val claim = getClaimDetails.execute(event.partition.claimId) ?: return
        val nearbyPlayers = getNearbyPlayers(claim)
        for (player in nearbyPlayers) {

            // Clear and redo visualisation for selected claim
            when (val result = getVisualisedClaimBlocks.execute(player.uniqueId, claim.id)) {
                is GetVisualisedClaimBlocksResult.Success -> {
                    visualiser.revertVisualisedBlocks(player, result.blockPositions)
                }
                else -> {}
            }
            unregisterVisualisation.execute(player.uniqueId, claim.id)
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