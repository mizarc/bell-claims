package dev.mizarc.bellclaims.application.actions.player.tool

import dev.mizarc.bellclaims.application.actions.player.visualisation.DisplayVisualisation
import dev.mizarc.bellclaims.application.actions.player.visualisation.IsPlayerVisualising
import dev.mizarc.bellclaims.application.actions.player.visualisation.ScheduleClearVisualisation
import dev.mizarc.bellclaims.application.results.player.visualisation.IsPlayerVisualisingResult
import dev.mizarc.bellclaims.application.services.ToolItemService
import dev.mizarc.bellclaims.domain.values.Position2D
import dev.mizarc.bellclaims.domain.values.Position3D
import java.util.UUID

class SyncToolVisualization(private val toolItemService: ToolItemService,
                                   private val displayVisualisation: DisplayVisualisation,
                                   private val scheduleClearVisualisation: ScheduleClearVisualisation,
                                   private val isPlayerVisualising: IsPlayerVisualising) {
    // Cache last known (holdingClaimTool, position) per player to avoid redundant work
    private val lastState: MutableMap<UUID, Pair<Boolean, Position2D>> = mutableMapOf()

    fun execute(playerId: UUID, position: Position3D,
                mainHandItemData: Map<String, String>?, offHandItemData: Map<String, String>?) {
        // Check if player is holding tool based on the item they're holding
        val holdingClaimTool = (toolItemService.isClaimTool(mainHandItemData)
                || (toolItemService.isClaimTool(offHandItemData)))

        // Skip if nothing relevant changed since the last execution for this player
        val last = lastState[playerId]
        val current = Pair(holdingClaimTool, Position2D(position.x, position.z))

        // Check if the player is holding the same tool and in the same chunk as before
        val lastHolding = last?.first
        val lastChunk = last?.second?.let { Pair(it.x shr 4, it.z shr 4) }
        val currentChunk = Pair(position.x shr 4, position.z shr 4)
        if (lastHolding != null && lastHolding == holdingClaimTool && lastChunk == currentChunk) return
        lastState[playerId] = current

        // Visualise or unvisualise depending on if player is holding claim tool
        if (holdingClaimTool) {
            displayVisualisation.execute(playerId, position)
        } else {
            // Only attempt to clear visualisation if the previous state was holding the claim tool
            val wasHoldingTool = last?.first == true
            if (wasHoldingTool) {
                val result = isPlayerVisualising.execute(playerId)
                if (result is IsPlayerVisualisingResult.Success && result.isVisualising) {
                    scheduleClearVisualisation.execute(playerId)
                }
            }
        }
    }

    // Optional helper to clear cached state for a player (callable from listeners on quit/world-change)
    fun clearCacheForPlayer(playerId: UUID) {
        lastState.remove(playerId)
    }
}