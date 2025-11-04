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
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.concurrent.thread

class EditToolVisualisingListener(private val plugin: JavaPlugin): Listener, KoinComponent {
    private val syncToolVisualization: SyncToolVisualization by inject()

    /**
     * Triggers when the player executes certain commands that modify their inventory
     * (e.g. /clear or /claim). We schedule a sync one tick later so the command
     * has already updated the player's inventory and we can correctly detect
     * whether the claim tool was added/removed from their hands.
     */
    @EventHandler
    fun onPlayerCommand(event: PlayerCommandPreprocessEvent) {
        event.player.sendMessage("testing")
        val message = event.message.lowercase()
        event.player.sendMessage(message)
        if (!message.startsWith("/clear") && !message.startsWith("/claim")) return
        val player = event.player
        event.player.sendMessage("got here")
        // Run one tick later so the command has taken effect
        plugin.server.scheduler.runTaskLater(plugin, Runnable { handleAutoVisualisation(player) }, 1L)
    }

    /**
     * Triggers when the player swaps to the claim tool in their inventory.
     */
    @EventHandler
    fun onHoldClaimTool(event: PlayerItemHeldEvent) {
        event.player.sendMessage("holding")
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