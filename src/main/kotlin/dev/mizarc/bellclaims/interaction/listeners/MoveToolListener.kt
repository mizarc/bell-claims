package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.api.PartitionService
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import dev.mizarc.bellclaims.domain.claims.ClaimRepository
import dev.mizarc.bellclaims.domain.partitions.Position3D
import java.util.*

class MoveToolListener(private val claimRepo: ClaimRepository,
                       private val partitionService: PartitionService): Listener {

    @EventHandler
    fun onClaimMoveBlockPlace(event: BlockPlaceEvent) {
        val claimId = event.itemInHand.itemMeta.persistentDataContainer.get(
            NamespacedKey("bellclaims","claim"), PersistentDataType.STRING) ?: return
        val claim = claimRepo.getById(UUID.fromString(claimId)) ?: return

        val partition = partitionService.getByLocation(event.blockPlaced.location)
        if (partition == null || partition.claimId != claim.id) {
            event.player.sendActionBar(
                Component.text("Place this block within the claim borders")
                .color(TextColor.color(255, 85, 85)))
            event.isCancelled = true
            return
        }

        val existingLocation = Location(claim.getWorld(),
            claim.position.x.toDouble(), claim.position.y.toDouble(), claim.position.z.toDouble())
        val existingBlock = existingLocation.block
        existingBlock.breakNaturally(ItemStack(Material.WOODEN_HOE))
        claim.position = Position3D(event.blockPlaced.location)
        claimRepo.update(claim)
        event.isCancelled = false
        event.player.sendActionBar(
            Component.text("Claim position has been moved")
                .color(TextColor.color(85, 255, 85)))
    }
}