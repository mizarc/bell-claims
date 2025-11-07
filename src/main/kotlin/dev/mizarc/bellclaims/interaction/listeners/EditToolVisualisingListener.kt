package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.application.actions.player.tool.SyncToolVisualization
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toCustomItemData
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toPosition3D
import io.papermc.paper.event.player.PlayerClientLoadedWorldEvent
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

class EditToolVisualisingListener(private val plugin: JavaPlugin): Listener, KoinComponent {
    private val syncToolVisualization: SyncToolVisualization by inject()

    // Track players who've just loaded in so we can ignore spurious inventory events until they're fully in-game
    private val initialisingPlayers: MutableSet<UUID> = mutableSetOf()

    @EventHandler
    fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        initialisingPlayers.add(event.uniqueId)
    }

    /**
     * Triggers when the player joins the server.
     */
    @EventHandler
    fun onPlayerJoin(event: PlayerClientLoadedWorldEvent) {
        val player = event.player
        handleAutoVisualisation(player)
        initialisingPlayers.remove(player.uniqueId)
    }

    /**
     * Triggers when an item in the player's inventory changes.
     */
    @EventHandler
    fun onInventoryItemChange(event: PlayerInventorySlotChangeEvent) {
        if (event.player.uniqueId in initialisingPlayers) return
        plugin.server.scheduler.runTask(plugin, Runnable { handleAutoVisualisation(event.player) })
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
        // prefer safe cast over checking entityType and then casting
        val player = event.entity as? Player ?: return
        plugin.server.scheduler.runTask(plugin, Runnable { handleAutoVisualisation(player) })
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
        val player = event.whoClicked as? Player ?: return
        plugin.server.scheduler.runTask(plugin, Runnable { handleAutoVisualisation(player) })
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        syncToolVisualization.clearCacheForPlayer(event.player.uniqueId)
        // Clean up initialising set in case player quit before the delayed task ran.
        initialisingPlayers.remove(event.player.uniqueId)
    }

    @EventHandler
    fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        syncToolVisualization.clearCacheForPlayer(event.player.uniqueId)
    }

    /**
     * Visualise if the player isn't already holding the claim tool (e.g. swapping hands)
     * Also update the tracked set depending on whether the player holds the tool after the sync.
     */
    private fun handleAutoVisualisation(player: Player) {
        val playerId = player.uniqueId
        val mainHand = player.inventory.itemInMainHand
        val offHand = player.inventory.itemInOffHand

        val mainData = mainHand.toCustomItemData()
        val offData = offHand.toCustomItemData()

        val position = player.location.toPosition3D()
        println("syncing")

        syncToolVisualization.execute(playerId, position, mainData, offData)
    }
}