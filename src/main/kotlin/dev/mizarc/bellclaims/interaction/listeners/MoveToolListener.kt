package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.application.actions.claim.anchor.MoveClaimAnchor
import dev.mizarc.bellclaims.application.actions.player.tool.GetClaimIdFromMoveTool
import dev.mizarc.bellclaims.application.results.claim.anchor.MoveClaimAnchorResult
import dev.mizarc.bellclaims.application.results.player.tool.GetClaimIdFromMoveToolResult
import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toCustomItemData
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toPosition3D
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

class MoveToolListener: Listener, KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()
    private val moveClaimAnchor: MoveClaimAnchor by inject()
    private val getClaimIdFromMoveTool: GetClaimIdFromMoveTool by inject()

    @EventHandler
    fun onClaimMoveBlockPlace(event: BlockPlaceEvent) {
        val playerId = event.player.uniqueId
        val result = getClaimIdFromMoveTool.execute(event.itemInHand.toCustomItemData())
        val claimId: UUID
        when (result) {
            GetClaimIdFromMoveToolResult.NotMoveTool -> return
            is GetClaimIdFromMoveToolResult.Success -> claimId = result.claimId
        }

        when (moveClaimAnchor.execute(
            claimId, event.player.uniqueId,
            event.blockPlaced.world.uid, event.blockPlaced.location.toPosition3D())) {
            is MoveClaimAnchorResult.Success -> {
                event.player.sendActionBar(
                    Component.text(localizationProvider.get(
                        event.player.uniqueId, LocalizationKeys.FEEDBACK_MOVE_TOOL_SUCCESS))
                        .color(TextColor.color(85, 255, 85)))
            }
            is MoveClaimAnchorResult.InvalidPosition -> {
                event.player.sendActionBar(
                    Component.text(localizationProvider.get(
                        playerId, LocalizationKeys.FEEDBACK_MOVE_TOOL_OUTSIDE_BORDER))
                        .color(TextColor.color(255, 85, 85)))
                event.isCancelled = true
            }
            is MoveClaimAnchorResult.NoPermission -> {
                event.player.sendActionBar(
                    Component.text(localizationProvider.get(playerId,
                        LocalizationKeys.FEEDBACK_MOVE_TOOL_NO_PERMISSION))
                        .color(TextColor.color(255, 85, 85)))
                event.player.inventory.setItemInMainHand(ItemStack.empty())
                event.isCancelled = true
            }
            is MoveClaimAnchorResult.StorageError -> {
                event.player.sendActionBar(
                    Component.text(localizationProvider.get(playerId,
                        LocalizationKeys.GENERAL_ERROR))
                        .color(TextColor.color(255, 85, 85)))
                event.player.inventory.setItemInMainHand(ItemStack.empty())
                event.isCancelled = true
            }
        }
    }
}