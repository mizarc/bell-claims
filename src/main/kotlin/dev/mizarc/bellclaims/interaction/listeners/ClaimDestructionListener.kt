package dev.mizarc.bellclaims.interaction.listeners

import com.destroystokyo.paper.event.block.BlockDestroyEvent
import dev.mizarc.bellclaims.application.actions.claim.anchor.BreakClaimAnchor
import dev.mizarc.bellclaims.application.actions.claim.anchor.GetClaimAnchorAtPosition
import dev.mizarc.bellclaims.application.actions.player.DoesPlayerHaveClaimOverride
import dev.mizarc.bellclaims.application.results.claim.anchor.BreakClaimAnchorResult
import dev.mizarc.bellclaims.application.results.claim.anchor.GetClaimAnchorAtPositionResult
import dev.mizarc.bellclaims.application.results.player.DoesPlayerHaveClaimOverrideResult
import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.block.data.type.Bell
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import dev.mizarc.bellclaims.domain.values.Position3D
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toLocation
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toPosition3D
import org.bukkit.Bukkit
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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

class ClaimDestructionListener: Listener, KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()
    private val getClaimAnchorAtPosition: GetClaimAnchorAtPosition by inject()
    private val breakClaimAnchor: BreakClaimAnchor by inject()
    private val doesPlayerHaveClaimOverride: DoesPlayerHaveClaimOverride by inject()

    @EventHandler
    fun onClaimHubDestroy(event: BlockBreakEvent) {
        if (event.block.blockData !is Bell) return
        val claim = when (
            val result = getClaimAnchorAtPosition.execute(event.block.location.toPosition3D(), event.block.world.uid)) {
            is GetClaimAnchorAtPositionResult.Success -> result.claim
            else -> return
        }

        val hasOverride = when (val result = doesPlayerHaveClaimOverride.execute(event.player.uniqueId)) {
            is DoesPlayerHaveClaimOverrideResult.Success -> result.hasOverride
            else -> false
        }

        // No permission to break bell
        val playerId = event.player.uniqueId
        if (playerId != claim.playerId && !hasOverride) {
            val playerName = Bukkit.getOfflinePlayer(claim.playerId).name ?:
                localizationProvider.get(playerId, LocalizationKeys.GENERAL_NAME_ERROR)
            event.player.sendActionBar(
                Component.text(localizationProvider.get(
                    playerId, LocalizationKeys.FEEDBACK_DESTRUCTION_PERMISSION, playerName))
                    .color(TextColor.color(255, 85, 85)))
            event.isCancelled = true
            return
        }

        when(val result = breakClaimAnchor.execute(event.block.world.uid, event.block.location.toPosition3D())) {
            is BreakClaimAnchorResult.ClaimBreaking -> {
                event.player.sendActionBar(
                    Component.text(localizationProvider.get(playerId, LocalizationKeys.FEEDBACK_DESTRUCTION_PENDING,
                        result.remainingBreaks, 10)).color(TextColor.color(255, 201, 14)))
                event.isCancelled = true
                return
            }
            is BreakClaimAnchorResult.Success -> {
                event.player.sendActionBar(
                    Component.text(localizationProvider.get(
                        playerId, LocalizationKeys.FEEDBACK_DESTRUCTION_SUCCESS, claim.name))
                        .color(TextColor.color(85, 255, 85)))
            }
            else -> {}
        }

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


    }

    @EventHandler
    fun onClaimHubAttachedDestroy(event: BlockBreakEvent) {
        if (wouldBlockBreakBell(event.block)) {
            event.player.sendActionBar(
                Component.text(localizationProvider.get(
                    event.player.uniqueId, LocalizationKeys.FEEDBACK_DESTRUCTION_ATTACHED))
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
            when (getClaimAnchorAtPosition.execute(block.location.toPosition3D(), block.location.world.uid)) {
                is GetClaimAnchorAtPositionResult.Success -> {
                    event.isCancelled = true
                    return
                }
                else -> {}
            }
        }
    }

    @EventHandler
    fun onPistolPull(event: BlockPistonRetractEvent) {
        for (block in event.blocks) {
            when (getClaimAnchorAtPosition.execute(block.location.toPosition3D(), block.location.world.uid)) {
                is GetClaimAnchorAtPositionResult.Success -> {
                    event.isCancelled = true
                    return
                }
                else -> {}
            }
        }
    }

    @EventHandler
    fun onTNTPrime(event: TNTPrimeEvent) {
        if (wouldBlockBreakBell(event.block)) {
            event.isCancelled = true

            val player = event.primingEntity as? Player ?: return
            player.sendActionBar(
                Component.text(localizationProvider.get(
                    player.uniqueId, LocalizationKeys.FEEDBACK_DESTRUCTION_ATTACHED))
                    .color(TextColor.color(255, 85, 85)))
        }
    }

    @EventHandler
    fun onBlockInteract(event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return
        if (wouldBlockBreakBell(block)) {
            event.player.sendActionBar(
                Component.text(localizationProvider.get(
                    event.player.uniqueId, LocalizationKeys.FEEDBACK_DESTRUCTION_ATTACHED))
                    .color(TextColor.color(255, 85, 85)))
            event.isCancelled = true
        }

        val door = block.blockData as? Bisected ?: return
        if (door.half == Bisected.Half.BOTTOM) {
            val otherLocation = block.location
            otherLocation.y = otherLocation.y + 1
            if (wouldBlockBreakBell(block.world.getBlockAt(otherLocation))) {
                event.player.sendActionBar(
                    Component.text(localizationProvider.get(
                        event.player.uniqueId, LocalizationKeys.FEEDBACK_DESTRUCTION_ATTACHED))
                        .color(TextColor.color(255, 85, 85)))
                event.isCancelled = true
            }
        }
        else {
            val otherLocation = block.location
            otherLocation.y = otherLocation.y - 1
            if (wouldBlockBreakBell(block.world.getBlockAt(otherLocation))) {
                event.player.sendActionBar(
                    Component.text(localizationProvider.get(
                        event.player.uniqueId, LocalizationKeys.FEEDBACK_DESTRUCTION_ATTACHED))
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
        when (getClaimAnchorAtPosition.execute(event.block.location.toPosition3D(), event.block.location.world.uid)) {
            is GetClaimAnchorAtPositionResult.Success -> {
                event.isCancelled = true
                return
            }
            else -> {}
        }
    }

    @EventHandler
    fun onTreeGrowth(event: StructureGrowEvent) {
        for (block in event.blocks) {
            when (getClaimAnchorAtPosition.execute(block.location.toPosition3D(), block.location.world.uid)) {
                is GetClaimAnchorAtPositionResult.Success -> {
                    event.isCancelled = true
                    return
                }
                else -> {}
            }
        }
    }

    fun explosionHandler(blocks: MutableList<Block>): List<Block> {
        val cancelledBlocks = mutableListOf<Block>()
        for (block in blocks) {
            when (getClaimAnchorAtPosition.execute(block.location.toPosition3D(), block.location.world.uid)) {
                is GetClaimAnchorAtPositionResult.Success -> cancelledBlocks.add(block)
                else -> {}
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
        for (position in getSurroundingPositions(block.location.toPosition3D())) {
            when (getClaimAnchorAtPosition.execute(block.location.toPosition3D(), block.location.world.uid)) {
                is GetClaimAnchorAtPositionResult.Success -> continue
                else -> {}
            }
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