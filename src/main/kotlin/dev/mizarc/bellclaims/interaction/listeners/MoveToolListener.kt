package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.application.actions.claim.anchor.MoveClaimAnchor
import dev.mizarc.bellclaims.application.results.claim.anchor.MoveClaimAnchorResult
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toPosition3D
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

class MoveToolListener: Listener, KoinComponent {
    private val moveClaimAnchor: MoveClaimAnchor by inject()

    @EventHandler
    fun onClaimMoveBlockPlace(event: BlockPlaceEvent) {
        val claimId = event.itemInHand.itemMeta.persistentDataContainer.get(
            NamespacedKey("bellclaims","claim"), PersistentDataType.STRING) ?: return

        when (moveClaimAnchor.execute(
            UUID.fromString(claimId), event.player.uniqueId,
            event.blockPlaced.world.uid, event.blockPlaced.location.toPosition3D())) {
            MoveClaimAnchorResult.Success -> {
                event.player.sendActionBar(
                    Component.text("Claim position has been moved")
                        .color(TextColor.color(85, 255, 85)))
                return
            }
            MoveClaimAnchorResult.InvalidPosition -> {
                event.player.sendActionBar(
                    Component.text("Place this block within the claim borders")
                        .color(TextColor.color(255, 85, 85)))
                event.isCancelled = true
                return
            }
            MoveClaimAnchorResult.NoPermission -> {
                event.player.sendActionBar(
                    Component.text("You cannot move this claim bell")
                        .color(TextColor.color(255, 85, 85)))
                event.player.inventory.setItemInMainHand(ItemStack.empty())
                event.isCancelled = true
                return
            }
            MoveClaimAnchorResult.StorageError -> {
                event.player.sendActionBar(
                    Component.text("An internal error has occurred, contact your local administrator.")
                        .color(TextColor.color(255, 85, 85)))
                event.player.inventory.setItemInMainHand(ItemStack.empty())
                event.isCancelled = true
                return
            }
        }
    }
}