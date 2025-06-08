package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.application.actions.player.tool.SyncToolVisualization
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toCustomItemData
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toPosition3D
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.concurrent.thread

class EditToolVisualisingListener(private val plugin: JavaPlugin): Listener, KoinComponent {
    private val syncToolVisualization: SyncToolVisualization by inject()

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
     * Triggers when the player clicks on a border block while visualisation is active.
     */
    @EventHandler
    fun onClaimToolClick(event: PlayerInteractEvent) {
        thread(start = true) {
            Thread.sleep(1)
            handleAutoVisualisation(event.player)
        }
    }

    /**
     * Visualise if player isn't already holding the claim tool (e.g. swapping hands)
     */
    private fun handleAutoVisualisation(player: Player) {
        val playerId = player.uniqueId
        val position = player.location.toPosition3D()
        val mainHand = player.inventory.itemInMainHand
        val offHand = player.inventory.itemInOffHand
        syncToolVisualization.execute(playerId, position, mainHand.toCustomItemData(), offHand.toCustomItemData())
    }
}