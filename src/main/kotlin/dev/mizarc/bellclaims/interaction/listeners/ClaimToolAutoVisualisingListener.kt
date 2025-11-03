package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.application.actions.player.tool.SyncToolVisualization
import dev.mizarc.bellclaims.application.services.ToolItemService
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toCustomItemData
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toPosition3D
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

/**
 * Automatically updates claim visualizations when a player crosses chunk borders while holding the
 * claim tool.
 */
class ClaimToolAutoVisualisingListener: Listener, KoinComponent {
    private val syncToolVisualization: SyncToolVisualization by inject()
    private val toolItemService: ToolItemService by inject()

    // Track the last chunk position per player to detect chunk border crossings
    private val lastChunkPosition: MutableMap<UUID, Pair<Int, Int>> = mutableMapOf()

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        // Early return if the player hasn't moved to another block
        val to = event.to
        val from = event.from
        if (from.world == to.world && from.blockX == to.blockX && from.blockZ == to.blockZ) return

        val player = event.player
        val playerId = player.uniqueId

        // Early return if the player is not holding the claim tool
        val mainHand = player.inventory.itemInMainHand
        val offHand = player.inventory.itemInOffHand
        val mainHandData = mainHand.toCustomItemData()
        val offHandData = offHand.toCustomItemData()
        if (!toolItemService.isClaimTool(mainHandData) && !toolItemService.isClaimTool(offHandData)) return

        // Check if the player crossed a chunk border
        val toChunkX = to.chunk.x
        val toChunkZ = to.chunk.z
        val lastChunk = lastChunkPosition[playerId]
        if (lastChunk != null) {
            val (lastChunkX, lastChunkZ) = lastChunk
            if (lastChunkX == toChunkX && lastChunkZ == toChunkZ) return
        }

        // Update tracked chunk position
        lastChunkPosition[playerId] = Pair(toChunkX, toChunkZ)

        // Update visualization directly
        val position = player.location.toPosition3D()
        syncToolVisualization.execute(playerId, position, mainHandData, offHandData)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        lastChunkPosition.remove(event.player.uniqueId)
    }

    @EventHandler
    fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        lastChunkPosition.remove(event.player.uniqueId)
    }
}
