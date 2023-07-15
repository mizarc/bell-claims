package xyz.mizarc.solidclaims.listeners

import org.bukkit.block.data.type.Bell
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import xyz.mizarc.solidclaims.ClaimQuery
import xyz.mizarc.solidclaims.partitions.Position3D

class ClaimDestructionListener(val claims: ClaimQuery): Listener {
    @EventHandler
    fun onClaimHubDestroy(event: BlockBreakEvent) {
        if (event.block.blockData !is Bell) return

        val claim = claims.claims.getByPosition(Position3D(event.block.location))
        if (claim != null) {
            claims.removeClaim(claim)
        }
    }
}