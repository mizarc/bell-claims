package dev.mizarc.bellclaims.interaction.listeners

import com.destroystokyo.paper.event.block.BlockDestroyEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.block.data.type.Bell
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import dev.mizarc.bellclaims.application.services.ClaimService
import dev.mizarc.bellclaims.application.services.ClaimWorldService
import dev.mizarc.bellclaims.application.services.PlayerStateService
import dev.mizarc.bellclaims.domain.partitions.Position3D
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.block.TNTPrimeEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.StructureGrowEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class ClaimDestructionListener(val claimService: ClaimService,
                               private val claimWorldService: ClaimWorldService,
                               private val playerStateService: PlayerStateService
): Listener {
    @EventHandler
    fun onClaimHubDestroy(event: BlockBreakEvent) {
        if (event.block.blockData !is Bell) return
        val claim = claimWorldService.getByLocation(event.block.location) ?: return

        val playerState = playerStateService.getByPlayer(event.player)

        // No permission to break bell
        if (event.player.uniqueId != claim.owner.uniqueId && playerState?.claimOverride != true) {
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

        claimService.destroy(claim)

        for ((index, item) in event.player.inventory.withIndex()) {
            if (item == null) continue
            val itemMeta = item.itemMeta ?: continue
            val claimText = itemMeta.persistentDataContainer.get(
                NamespacedKey("bellclaims","claim"), PersistentDataType.STRING) ?: continue
            val claimId = UUID.fromString(claimText) ?: continue
            if (claimId == claim.id) {
                if (index == 40) {
                    event.player.inventory.setItemInOffHand(ItemStack(Material.AIR))
                }
                else {
                    event.player.inventory.remove(item)
                }
            }
        }

        event.player.sendActionBar(
            Component.text("Claim '${claim.name}' has been destroyed")
            .color(TextColor.color(85, 255, 85)))
    }

    @EventHandler
    fun onClaimHubAttachedDestroy(event: BlockBreakEvent) {
        if (wouldBlockBreakBell(event.block)) {
            event.player.sendActionBar(
                Component.text("That block is attached to the claim bell")
                    .color(TextColor.color(255, 85, 85)))
            event.isCancelled = true
            return
        }
    }

    @EventHandler
    fun onBlockExplode(event: BlockExplodeEvent) {
        val blocks = explosionHandler(event.blockList())
        event.blockList().removeAll(blocks)
    }

    @EventHandler
    fun onEntityExplode(event: EntityExplodeEvent) {
        val blocks = explosionHandler(event.blockList())
        event.blockList().removeAll(blocks)
    }

    @EventHandler
    fun onPistolPush(event: BlockPistonExtendEvent) {
        for (block in event.blocks) {
            if (claimWorldService.getByLocation(block.location) != null) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPistolPull(event: BlockPistonRetractEvent) {
        for (block in event.blocks) {
            if (claimWorldService.getByLocation(block.location) != null) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onTNTPrime(event: TNTPrimeEvent) {
        if (wouldBlockBreakBell(event.block)) {
            event.isCancelled = true

            val player = event.primingEntity as? Player ?: return
            player.sendActionBar(
                Component.text("That block is attached to the claim bell")
                    .color(TextColor.color(255, 85, 85)))
        }
    }

    @EventHandler
    fun onBlockInteract(event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return
        if (wouldBlockBreakBell(block)) {
            event.player.sendActionBar(
                Component.text("That block is attached to the claim bell")
                    .color(TextColor.color(255, 85, 85)))
            event.isCancelled = true
        }

        val door = block.blockData as? Bisected ?: return
        if (door.half == Bisected.Half.BOTTOM) {
            val otherLocation = block.location
            otherLocation.y = otherLocation.y + 1
            if (wouldBlockBreakBell(block.world.getBlockAt(otherLocation))) {
                event.player.sendActionBar(
                    Component.text("That block is attached to the claim bell")
                        .color(TextColor.color(255, 85, 85)))
                event.isCancelled = true
            }
        }
        else {
            val otherLocation = block.location
            otherLocation.y = otherLocation.y - 1
            if (wouldBlockBreakBell(block.world.getBlockAt(otherLocation))) {
                event.player.sendActionBar(
                    Component.text("That block is attached to the claim bell")
                        .color(TextColor.color(255, 85, 85)))
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onBlockFade(event: BlockFadeEvent) {
        if (wouldBlockBreakBell(event.newState.block)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onBlockDestroy(event: BlockDestroyEvent) {
        if (claimWorldService.getByLocation(event.block.location) != null) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onTreeGrowth(event: StructureGrowEvent) {
        for (block in event.blocks) {
            if (claimWorldService.getByLocation(block.location) != null) {
                event.isCancelled = true
            }
        }
    }

    fun explosionHandler(blocks: MutableList<Block>): List<Block> {
        val cancelledBlocks = mutableListOf<Block>()
        for (block in blocks) {
            if (claimWorldService.getByLocation(block.location) != null) {
                cancelledBlocks.add(block)
            }

            if (wouldBlockBreakBell(block)) {
                cancelledBlocks.add(block)
            }
        }
        return cancelledBlocks
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

    fun wouldBlockBreakBell(block: Block): Boolean {
        for (position in getSurroundingPositions(Position3D(block.location))) {
            claimWorldService.getByLocation(position.toLocation(block.world)) ?: continue
            val blockAt = block.world.getBlockAt(position.toLocation(block.world))
            val bell = blockAt.blockData as? Bell ?: continue

            if (bell.attachment == Bell.Attachment.CEILING && blockAt.getRelative(BlockFace.UP) == block ||
                bell.attachment == Bell.Attachment.FLOOR && blockAt.getRelative(BlockFace.DOWN) == block ||
                bell.attachment == Bell.Attachment.SINGLE_WALL &&
                (bell.facing == BlockFace.EAST && blockAt.getRelative(BlockFace.EAST) == block ||
                        bell.facing == BlockFace.WEST && blockAt.getRelative(BlockFace.WEST) == block ||
                        bell.facing == BlockFace.NORTH && blockAt.getRelative(BlockFace.NORTH) == block ||
                        bell.facing == BlockFace.SOUTH && blockAt.getRelative(BlockFace.SOUTH) == block)) {
                return true
            }
        }
        return false
    }
}