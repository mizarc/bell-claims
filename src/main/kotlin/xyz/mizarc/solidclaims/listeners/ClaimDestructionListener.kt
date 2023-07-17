package xyz.mizarc.solidclaims.listeners

import org.bukkit.block.data.type.Bell
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import xyz.mizarc.solidclaims.ClaimService
import xyz.mizarc.solidclaims.PartitionService
import xyz.mizarc.solidclaims.partitions.Position3D

class ClaimDestructionListener(val claimService: ClaimService): Listener {
    @EventHandler
    fun onClaimHubDestroy(event: BlockBreakEvent) {
        if (event.block.blockData !is Bell) return

        val claim = claimService.getByLocation(event.block.location)
        if (claim != null) {
            claimService.removeClaim(claim)
        }
    }
}