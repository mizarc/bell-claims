package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.api.PlayerStateService
import dev.mizarc.bellclaims.domain.partitions.Position3D
import dev.mizarc.bellclaims.domain.players.PlayerState
import dev.mizarc.bellclaims.infrastructure.getClaimTool
import dev.mizarc.bellclaims.infrastructure.persistence.Config
import dev.mizarc.bellclaims.interaction.visualisation.Visualiser
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
import kotlin.concurrent.thread

class EditToolVisualisingListener(private val plugin: JavaPlugin,
                                  private val playerStateService: PlayerStateService,
                                  private val visualiser: Visualiser,
                                  private val config: Config): Listener {
    @EventHandler
    fun onHoldClaimTool(event: PlayerItemHeldEvent) {
        plugin.server.scheduler.runTask(plugin, Runnable { autoClaimToolVisualisation(event.player) })
    }

    @EventHandler
    fun onPickupClaimTool(event: EntityPickupItemEvent) {
        if (event.entityType != EntityType.PLAYER) return
        plugin.server.scheduler.runTask(plugin, Runnable { autoClaimToolVisualisation(event.entity as Player) })
    }

    @EventHandler
    fun onDropClaimTool(event: PlayerDropItemEvent) {
        plugin.server.scheduler.runTask(plugin, Runnable { autoClaimToolVisualisation(event.player) })
    }

    @EventHandler
    fun onClaimToolInventoryInteract(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        plugin.server.scheduler.runTask(plugin, Runnable { autoClaimToolVisualisation(player) })
    }

    @EventHandler
    fun onBorderClick(event: PlayerInteractEvent) {
        val playerState = playerStateService.getByPlayer(event.player) ?: return
        val clickedBlock = event.clickedBlock ?: return
        for (claim in playerState.visualisedBlockPositions) {
            if (claim.value.contains(Position3D(clickedBlock.location))) {
                thread(start = true) {
                    Thread.sleep(1)
                    val blockPositions = playerState.visualisedBlockPositions[claim.key] ?: return@thread
                    blockPositions.toMutableSet().remove(Position3D(clickedBlock.location))
                    playerState.visualisedBlockPositions[claim.key] = blockPositions
                    visualiser.refresh(event.player)
                }
                return
            }
        }
    }

    private fun autoClaimToolVisualisation(player: Player) {
        val mainItemMeta = player.inventory.itemInMainHand.itemMeta
        val offhandItemMeta = player.inventory.itemInOffHand.itemMeta
        val playerState = playerStateService.getByPlayer(player) ?: return

        // Visualise if player isn't already holding the claim tool (e.g. swapping hands)
        val holdingClaimTool = (mainItemMeta == getClaimTool().itemMeta) || (offhandItemMeta == getClaimTool().itemMeta)
        if (!holdingClaimTool || !playerState.isHoldingClaimTool) {
            if (config.visualiserDelayPeriod > 0) {
                runDelayedVisualisation(holdingClaimTool, playerState, player)
            }
            else {
                visualiser.hide(player)
                if (holdingClaimTool) visualiser.show(player)
            }
        }
        playerState.isHoldingClaimTool = holdingClaimTool
    }

    private fun runDelayedVisualisation(holdingClaimTool: Boolean, playerState: PlayerState, player: Player) {
        if (holdingClaimTool) {
            val scheduledVisualiserHide = playerState.scheduledVisualiserHide
            if (scheduledVisualiserHide != null && !scheduledVisualiserHide.isCancelled) {
                scheduledVisualiserHide.cancel()
                playerState.scheduledVisualiserHide = null
            } else {
                visualiser.show(player)
            }

        } else {
            visualiser.delayedVisualiserHide(player)
        }
    }
}