package dev.mizarc.bellclaims.listeners

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.block.data.type.Bell
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import dev.mizarc.bellclaims.ClaimService
import dev.mizarc.bellclaims.PartitionService
import dev.mizarc.bellclaims.partitions.Position2D
import dev.mizarc.bellclaims.partitions.Position3D
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace

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

    @EventHandler
    fun onClaimHubAttachedDestroy(event: BlockBreakEvent) {
        for (position in getSurroundingPositions(Position3D(event.block.location))) {
            claimService.getByLocation(position.toLocation(event.block.world)) ?: continue
            val blockAt = event.block.world.getBlockAt(position.toLocation(event.block.world))
            val bell = blockAt.blockData as Bell

            if (bell.attachment == Bell.Attachment.CEILING && blockAt.getRelative(BlockFace.UP) == event.block ||
                    bell.attachment == Bell.Attachment.FLOOR && blockAt.getRelative(BlockFace.DOWN) == event.block ||
                    bell.attachment == Bell.Attachment.SINGLE_WALL &&
                    (bell.facing == BlockFace.EAST && blockAt.getRelative(BlockFace.EAST) == event.block ||
                    bell.facing == BlockFace.WEST && blockAt.getRelative(BlockFace.WEST) == event.block) ||
                    bell.facing == BlockFace.NORTH && blockAt.getRelative(BlockFace.NORTH) == event.block ||
                    bell.facing == BlockFace.SOUTH && blockAt.getRelative(BlockFace.SOUTH) == event.block) {
                event.player.sendActionBar(
                    Component.text("Can't destroy block claim bell is attached to.")
                        .color(TextColor.color(255, 85, 85)))
                event.isCancelled = true
                return
            }
        }
    }

    fun getSurroundingPositions(position: Position3D): List<Position3D> {
        val positions = mutableListOf<Position3D>()

        for (x in -1..1) {
            for (y in -1..1) {
                for (z in -1..1) {
                    positions.add(Position3D(position.x + x, position.y + y, position.z + z))
                }
            }
        }
        return positions
    }
}