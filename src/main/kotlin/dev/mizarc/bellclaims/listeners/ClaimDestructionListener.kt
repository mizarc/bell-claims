package dev.mizarc.bellclaims.listeners

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.block.data.type.Bell
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import dev.mizarc.bellclaims.ClaimService
import dev.mizarc.bellclaims.PartitionService
import dev.mizarc.bellclaims.partitions.Position3D

class ClaimDestructionListener(val claimService: ClaimService): Listener {
    @EventHandler
    fun onClaimHubDestroy(event: BlockBreakEvent) {
        if (event.block.blockData !is Bell) return

        val claim = claimService.getByLocation(event.block.location) ?: return

        if (event.player.uniqueId != claim.owner.uniqueId) {
            event.player.sendActionBar(
                Component.text("This claim belongs to ${claim.owner.name}")
                    .color(TextColor.color(255, 85, 85)))
            event.isCancelled = true
            return
        }

        claim.resetBreakCount()

        if (claim.breakCount > 1) {
            claim.breakCount -= 1
            event.player.sendActionBar(
                Component.text("Break ${claim.breakCount} more times in 10 seconds to destroy this claim")
                .color(TextColor.color(255, 201, 14)))
            event.isCancelled = true
            return
        }

        claimService.removeClaim(claim)
        event.player.sendActionBar(
            Component.text("Claim '${claim.name}' has been destroyed")
            .color(TextColor.color(85, 255, 85)))
    }
}