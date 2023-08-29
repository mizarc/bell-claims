package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.api.ClaimService
import dev.mizarc.bellclaims.api.PartitionService
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockSpreadEvent
import org.bukkit.event.world.StructureGrowEvent

class MiscPreventions(private val claimService: ClaimService,
                      private val partitionService: PartitionService) : Listener {
    @EventHandler
    fun onTreeGrowth(event: StructureGrowEvent) {
        if (partitionService.getByLocation(event.location) != null) {
            val partition = partitionService.getByLocation(event.location) ?: return
            val claim = claimService.getById(partition.claimId) ?: return
            for (block in event.blocks) {
                if (partitionService.getByLocation(block.location) != null) {
                    val otherPartition = partitionService.getByLocation(block.location) ?: continue
                    val otherClaim = claimService.getById(otherPartition.claimId) ?: continue
                    if (claim.id != otherClaim.id) {
                        partitionService.getByLocation(block.location) ?: continue
                        event.isCancelled = true
                        return
                    }
                }
            }
            return
        }

        for (block in event.blocks) {
            if (partitionService.getByLocation(block.location) != null) {
                event.isCancelled = true
                return
            }
        }
    }

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