package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.application.actions.player.tool.SyncToolVisualization
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toCustomItemData
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toPosition3D
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

/**
 * Automatically updates claim visualizations when a player crosses chunk borders while holding the
 * claim tool.
 */
class ClaimToolAutoVisualisingListener(private val plugin: JavaPlugin): Listener, KoinComponent {
    private val syncToolVisualization: SyncToolVisualization by inject()

    // Track the last chunk position per player to detect chunk border crossings
    private val lastChunkPosition: MutableMap<UUID, Pair<Int, Int>> = mutableMapOf()

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val to = event.to
        val from = event.from

        // Early return if the player hasn't moved to another block
        if (from.world == to.world && from.blockX == to.blockX && from.blockZ == to.blockZ) return

        val player = event.player
        val playerId = player.uniqueId
        val toChunkX = to.chunk.x
        val toChunkZ = to.chunk.z

        // Check if the player crossed a chunk border
        val lastChunk = lastChunkPosition[playerId]
        if (lastChunk != null) {
            val (lastChunkX, lastChunkZ) = lastChunk
            if (lastChunkX == toChunkX && lastChunkZ == toChunkZ) return
        }

        // Update tracked chunk position
        lastChunkPosition[playerId] = Pair(toChunkX, toChunkZ)

        // Schedule visualization update on main thread
        plugin.server.scheduler.runTask(plugin, Runnable {
            handleAutoVisualisationUpdate(player)
        })
    }

    /**
     * Update the visualisation if the player is holding the claim tool
     */
    private fun handleAutoVisualisationUpdate(player: Player) {
        val playerId = player.uniqueId
        val position = player.location.toPosition3D()
        val mainHand = player.inventory.itemInMainHand
        val offHand = player.inventory.itemInOffHand
        syncToolVisualization.execute(playerId, position, mainHand.toCustomItemData(), offHand.toCustomItemData())
    }
}
