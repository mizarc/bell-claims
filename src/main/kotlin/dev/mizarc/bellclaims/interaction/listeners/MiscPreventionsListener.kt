package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.api.ClaimService
import dev.mizarc.bellclaims.api.PartitionService
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockSpreadEvent
import org.bukkit.event.world.StructureGrowEvent

class MiscPreventionsListener(private val claimService: ClaimService,
                              private val partitionService: PartitionService) : Listener {

    @EventHandler
    fun onSculkSpread(event: BlockSpreadEvent) {
        if (event.source.type == Material.SCULK_CATALYST) {
            val partition = partitionService.getByLocation(event.block.location)
            val sourcePartition = partitionService.getByLocation(event.source.location)

            if (sourcePartition == partition) {
                return
            }

            if (sourcePartition == null) {
                event.isCancelled = true
            }
        }
    }
}