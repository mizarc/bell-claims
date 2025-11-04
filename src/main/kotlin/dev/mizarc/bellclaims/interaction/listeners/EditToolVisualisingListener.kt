package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.application.actions.player.tool.SyncToolVisualization
import dev.mizarc.bellclaims.application.persistence.PlayerStateRepository
import dev.mizarc.bellclaims.application.services.ToolItemService
import dev.mizarc.bellclaims.config.MainConfig
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toCustomItemData
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toPosition3D
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.max

class EditToolVisualisingListener(private val plugin: JavaPlugin): Listener, KoinComponent {
    private val syncToolVisualization: SyncToolVisualization by inject()
    private val config: MainConfig by inject()
    private val toolItemService: ToolItemService by inject()
    private val playerStateRepository: PlayerStateRepository by inject()

    init {
        // Start a periodic poll to catch programmatic inventory changes that don't emit
        // a convenient event (other plugins calling Inventory.clear(), API updates, etc).
        if (config.autoRefreshVisualisation) {
            val intervalTicks = if (config.visualiserRefreshPeriod > 0.0)
                max(1L, (config.visualiserRefreshPeriod * 20.0).toLong())
            else 20L

            plugin.server.scheduler.runTaskTimer(plugin, Runnable {
                pollTrackedPlayersForToolChanges()
            }, intervalTicks, intervalTicks)
        }
    }

    /**
     * Triggers when the player swaps to the claim tool in their inventory.
     */
    @EventHandler
    fun onHoldClaimTool(event: PlayerItemHeldEvent) {
        plugin.server.scheduler.runTask(plugin, Runnable { handleAutoVisualisation(event.player) })
    }

    /**
     * Triggers when the player picks up the claim tool.
     */
    @EventHandler
    fun onPickupClaimTool(event: EntityPickupItemEvent) {
        if (event.entityType != EntityType.PLAYER) return
        plugin.server.scheduler.runTask(plugin, Runnable { handleAutoVisualisation(event.entity as Player) })
    }

    /**
     * Triggers when the player drops the claim tool.
     */
    @EventHandler
    fun onDropClaimTool(event: PlayerDropItemEvent) {
        plugin.server.scheduler.runTask(plugin, Runnable { handleAutoVisualisation(event.player) })
    }

    /**
     * Triggers when the player clicks on the claim tool in their inventory.
     */
    @EventHandler
    fun onClaimToolInventoryInteract(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        plugin.server.scheduler.runTask(plugin, Runnable { handleAutoVisualisation(player) })
    }

    /**
     * Poll only players that are tracked in PlayerStateRepository as holding the claim tool.
     */
    private fun pollTrackedPlayersForToolChanges() {
        val tracked = playerStateRepository.getAll().filter { it.isHoldingClaimTool }
        for (playerState in tracked) {
            val player = plugin.server.getPlayer(playerState.playerId) ?: continue
            if (!player.isOnline) continue
            handleAutoVisualisation(player)
        }
    }

    /**
     * Visualise if player isn't already holding the claim tool (e.g. swapping hands)
     * Also update the tracked set depending on whether the player holds the tool after the sync.
     */
    private fun handleAutoVisualisation(player: Player) {
        val playerId = player.uniqueId
        val position = player.location.toPosition3D()
        val mainHand = player.inventory.itemInMainHand
        val offHand = player.inventory.itemInOffHand

        val mainData = mainHand.toCustomItemData()
        val offData = offHand.toCustomItemData()

        syncToolVisualization.execute(playerId, position, mainData, offData)
    }
}