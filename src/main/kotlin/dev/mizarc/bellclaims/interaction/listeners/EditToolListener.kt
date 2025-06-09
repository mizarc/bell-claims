package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.application.actions.claim.GetClaimAtPosition
import dev.mizarc.bellclaims.application.actions.claim.partition.CreatePartition
import dev.mizarc.bellclaims.application.actions.claim.partition.GetPartitionByPosition
import dev.mizarc.bellclaims.application.actions.claim.partition.ResizePartition
import dev.mizarc.bellclaims.application.actions.player.DoesPlayerHaveClaimOverride
import dev.mizarc.bellclaims.application.actions.player.GetRemainingClaimBlockCount
import dev.mizarc.bellclaims.application.actions.player.tool.IsItemClaimTool
import dev.mizarc.bellclaims.application.actions.player.visualisation.DisplayVisualisation
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.EquipmentSlot
import dev.mizarc.bellclaims.application.events.PartitionModificationEvent
import dev.mizarc.bellclaims.application.results.claim.GetClaimAtPositionResult
import dev.mizarc.bellclaims.application.results.claim.partition.CreatePartitionResult
import dev.mizarc.bellclaims.application.results.claim.partition.ResizePartitionResult
import dev.mizarc.bellclaims.application.results.player.DoesPlayerHaveClaimOverrideResult
import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.values.Position2D
import dev.mizarc.bellclaims.interaction.menus.misc.EditToolMenu
import dev.mizarc.bellclaims.domain.values.Area
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toCustomItemData
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toPosition2D
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toPosition3D
import dev.mizarc.bellclaims.interaction.menus.MenuNavigator
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

/**
 * Actions based on utilizing the claim tool.
 */
class EditToolListener: Listener, KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()
    private val getPartitionByPosition: GetPartitionByPosition by inject()
    private val displayVisualisation: DisplayVisualisation by inject()
    private val createPartition: CreatePartition by inject()
    private val getRemainingClaimBlockCount: GetRemainingClaimBlockCount by inject()
    private val getClaimAtPosition: GetClaimAtPosition by inject()
    private val doesPlayerHaveClaimOverride: DoesPlayerHaveClaimOverride by inject()
    private val resizePartition: ResizePartition by inject()
    private val isItemClaimTool: IsItemClaimTool by inject()

    // Map of player id to the partition and the first selected corner to resize a partition
    private val firstSelectedCornerResize: MutableMap<UUID, Pair<UUID, Position2D>> = mutableMapOf()

    // Map of player id to the claim and the first selected corner to create a partition
    private val firstSelectedCornerCreate: MutableMap<UUID, Pair<UUID, Position2D>> = mutableMapOf()

    @EventHandler
    fun onUseClaimTool(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val item = event.item ?: return
        val clickedBlock = event.clickedBlock ?: return
        if (!isItemClaimTool.execute(item.toCustomItemData())) return

        // Open the menu if in offhand
        if (event.hand == EquipmentSlot.OFF_HAND) {
            val location = event.clickedBlock?.location ?: return
            val partition = getPartitionByPosition.execute(location.toPosition2D(), location.world.uid)
            val menuNavigator = MenuNavigator(event.player)
            menuNavigator.openMenu(EditToolMenu(menuNavigator, event.player, partition))
            return
        }

        displayVisualisation.execute(event.player.uniqueId, event.player.location.toPosition3D())

        // Resizes an existing partition
        val partitionResizer = firstSelectedCornerResize[event.player.uniqueId]
        if (partitionResizer != null) {
            resizePartitionBranch(event.player, clickedBlock.location, partitionResizer)
            return
        }

        // Creates a new partition
        val partitionBuilder = firstSelectedCornerCreate[event.player.uniqueId]
        if (partitionBuilder != null) {
            createPartitionBranch(event.player, clickedBlock.location, partitionBuilder)
            return
        }

        // Select corner of existing claim for resize operation
        if (selectExistingCorner(event.player, clickedBlock.location)) {
            return
        }

        // Selects a fresh location to start a new claim
        selectNewCorner(event.player, clickedBlock.location)
    }

    @EventHandler
    fun onToolSwitch(event: PlayerItemHeldEvent) {
        val item = event.player.inventory.getItem(event.previousSlot) ?: return
        if (!isItemClaimTool.execute(item.toCustomItemData())) return

        // Cancel claim building on unequip
        val partitionBuilder = firstSelectedCornerResize[event.player.uniqueId]
        if (partitionBuilder != null) {
            firstSelectedCornerResize.remove(event.player.uniqueId)
            event.player.sendActionBar(
                Component.text(localizationProvider.get(event.player.uniqueId,
                    LocalizationKeys.FEEDBACK_EDIT_TOOL_UNEQUIP_BUILD))
                .color(TextColor.color(255, 85, 85)))
            return
        }

        // Cancel claim resizing
        val partitionResizer = firstSelectedCornerCreate[event.player.uniqueId]
        if (partitionResizer != null) {
            firstSelectedCornerCreate.remove(event.player.uniqueId)
            event.player.sendActionBar(
                Component.text(localizationProvider.get(
                    event.player.uniqueId, LocalizationKeys.FEEDBACK_EDIT_TOOL_UNEQUIP_RESIZE))
                    .color(TextColor.color(255, 85, 85)))
        }
    }

    /**
     * Selects the corner of the claim that is going to be resized.
     */
    fun selectNewCorner(player: Player, location: Location) {
        // Check if the selected spot exists in an existing claim
        val partition = getPartitionByPosition.execute(location.toPosition2D(), location.world.uid)
        if (partition != null) {
            player.sendActionBar(
                Component.text(localizationProvider.get(
                    player.uniqueId, LocalizationKeys.FEEDBACK_EDIT_TOOL_IN_CLAIM))
                    .color(TextColor.color(255, 85, 85)))
            return
        }

        // Find claims next to the current selection
        var selectedClaim: Claim? = null
        val adjacentClaims = findAdjacentClaims(location)
        for (claim in adjacentClaims) {
            if (claim.playerId == player.uniqueId) {
                selectedClaim = claim
                break
            }
        }

        // Check if selection exists next to any of the player's owned claims
        if (selectedClaim == null) {
            return player.sendActionBar(
                Component.text(localizationProvider.get(
                    player.uniqueId, LocalizationKeys.FEEDBACK_EDIT_TOOL_INVALID))
                    .color(TextColor.color(255, 85, 85)))
        }

        val remainingClaimBlockCount = getRemainingClaimBlockCount.execute(player.uniqueId)

        // Check if the player already hit claim block limit
        if (remainingClaimBlockCount < 1) {
            return player.sendActionBar(
                Component.text(localizationProvider.get(
                    player.uniqueId, LocalizationKeys.FEEDBACK_EDIT_TOOL_INSUFFICIENT))
                    .color(TextColor.color(255, 85, 85)))
        }

        // Start partition building
        firstSelectedCornerCreate[player.uniqueId] = Pair(selectedClaim.id, location.toPosition2D())
        return player.sendActionBar(
            Component.text(localizationProvider.get(
                player.uniqueId, LocalizationKeys.FEEDBACK_EDIT_TOOL_START_EXTENSION,
                remainingClaimBlockCount)).color(TextColor.color(85, 255, 85)))
    }

    /**
     * Creates a new partition using a claim builder.
     */
    fun createPartitionBranch(player: Player, location: Location, partitionBuilder: Pair<UUID, Position2D>) {
        val secondPosition = Position2D(location.x.toInt(), location.z.toInt())
        val area = Area(partitionBuilder.second, secondPosition)
        when (val result = createPartition.execute(partitionBuilder.first, area)) {
            is CreatePartitionResult.Success -> {
                player.sendActionBar(Component.text(localizationProvider.get(
                    player.uniqueId, LocalizationKeys.FEEDBACK_EDIT_TOOL_NEW_PARTITION, result.claim.name, 0))
                    .color(TextColor.color(85, 255, 85)))
                firstSelectedCornerCreate.remove(player.uniqueId)
                val event = PartitionModificationEvent(result.partition)
                event.callEvent()
            }
            is CreatePartitionResult.Disconnected -> {
                player.sendActionBar(Component.text(localizationProvider.get(
                    player.uniqueId, LocalizationKeys.FEEDBACK_EDIT_TOOL_NOT_CONNECTED))
                    .color(TextColor.color(255, 85, 85)))
            }
            is CreatePartitionResult.InsufficientBlocks -> {
                player.sendActionBar(Component.text(localizationProvider.get(
                    player.uniqueId, LocalizationKeys.FEEDBACK_EDIT_TOOL_INSUFFICIENT, result.requiredExtraBlocks))
                    .color(TextColor.color(255, 85, 85)))
            }
            is CreatePartitionResult.Overlaps -> {
                player.sendActionBar(Component.text(localizationProvider.get(
                    player.uniqueId, LocalizationKeys.FEEDBACK_EDIT_TOOL_OVERLAP))
                    .color(TextColor.color(255, 85, 85)))
            }
            is CreatePartitionResult.TooClose -> {
                player.sendActionBar(Component.text(localizationProvider.get(
                    player.uniqueId, LocalizationKeys.FEEDBACK_EDIT_TOOL_TOO_CLOSE))
                    .color(TextColor.color(255, 85, 85)))
            }
            is CreatePartitionResult.TooSmall -> {
                player.sendActionBar(Component.text(localizationProvider.get(
                    player.uniqueId, LocalizationKeys.FEEDBACK_EDIT_TOOL_MINIMUM_SIZE, result.minimumSize))
                    .color(TextColor.color(255, 85, 85)))
            }
            is CreatePartitionResult.StorageError -> {
                player.sendActionBar(Component.text(localizationProvider.get(
                    player.uniqueId, LocalizationKeys.GENERAL_ERROR))
                    .color(TextColor.color(255, 85, 85)))
            }
        }
    }

    /**
     * Selects an existing claim corner if one is selected that a player has access to.
     */
    fun selectExistingCorner(player: Player, location: Location) : Boolean {
        val partition = getPartitionByPosition.execute(location.toPosition2D(), location.world.uid) ?: return false
        val claim = when (val result = getClaimAtPosition.execute(location.world.uid, location.toPosition2D())) {
            is GetClaimAtPositionResult.Success -> result.claim
            else -> return false
        }

        // Check for permission to modify claim.
        val hasOverride = when (doesPlayerHaveClaimOverride.execute(player.uniqueId)) {
            DoesPlayerHaveClaimOverrideResult.StorageError -> false
            is DoesPlayerHaveClaimOverrideResult.Success -> true
        }
        if (hasOverride) {}
        else if (claim.playerId != player.uniqueId) {
            player.sendActionBar(
                Component.text(localizationProvider.get(
                    player.uniqueId, LocalizationKeys.FEEDBACK_EDIT_TOOL_PERMISSION))
                    .color(TextColor.color(255, 85, 85)))
            return false
        }

        if (!partition.isPositionInCorner(location.toPosition2D())) {
            return false
        }

        firstSelectedCornerResize[player.uniqueId] = Pair(partition.id, location.toPosition2D())
        val remainingClaimBlockCount = getRemainingClaimBlockCount.execute(player.uniqueId)
        player.sendActionBar(
            Component.text(localizationProvider.get(
                player.uniqueId, LocalizationKeys.FEEDBACK_EDIT_TOOL_START_RESIZE, remainingClaimBlockCount))
                .color(TextColor.color(85, 255, 85)))
        return true
    }

    /**
     * Selects a new position to resize the claim.
     */
    fun resizePartitionBranch(player: Player, location: Location, partitionResizer: Pair<UUID, Position2D>) {
        when (val result = resizePartition.execute(partitionResizer.first, partitionResizer.second,
                location.toPosition2D())) {
            is ResizePartitionResult.Success -> {
                player.sendActionBar(
                    Component.text(localizationProvider.get(
                        player.uniqueId, LocalizationKeys.FEEDBACK_EDIT_TOOL_SUCCESSFUL_RESIZE, result.remainingBlocks))
                        .color(TextColor.color(85, 255, 85)))
                val event = PartitionModificationEvent(result.partition)
                event.callEvent()
                firstSelectedCornerResize.remove(player.uniqueId)
            }
            is ResizePartitionResult.Disconnected -> {
                player.sendActionBar(Component.text(localizationProvider.get(
                    player.uniqueId, LocalizationKeys.FEEDBACK_EDIT_TOOL_NOT_CONNECTED))
                    .color(TextColor.color(255, 85, 85)))
            }
            is ResizePartitionResult.ExposedClaimAnchor -> {
                player.sendActionBar(Component.text(localizationProvider.get(
                    player.uniqueId, LocalizationKeys.FEEDBACK_EDIT_TOOL_IN_CLAIM))
                    .color(TextColor.color(255, 85, 85)))
            }
            is ResizePartitionResult.InsufficientBlocks -> {
                player.sendActionBar(Component.text(localizationProvider.get(
                    player.uniqueId, LocalizationKeys.FEEDBACK_EDIT_TOOL_INSUFFICIENT))
                    .color(TextColor.color(255, 85, 85)))
            }
            is ResizePartitionResult.Overlaps -> {
                player.sendActionBar(Component.text(localizationProvider.get(
                    player.uniqueId, LocalizationKeys.FEEDBACK_EDIT_TOOL_OVERLAP))
                    .color(TextColor.color(255, 85, 85)))
            }
            is ResizePartitionResult.TooClose -> {
                player.sendActionBar(Component.text(localizationProvider.get(
                    player.uniqueId, LocalizationKeys.FEEDBACK_EDIT_TOOL_TOO_CLOSE))
                    .color(TextColor.color(255, 85, 85)))
            }
            is ResizePartitionResult.TooSmall -> {
                player.sendActionBar(Component.text(localizationProvider.get(
                    player.uniqueId, LocalizationKeys.FEEDBACK_EDIT_TOOL_MINIMUM_SIZE, result.minimumSize))
                    .color(TextColor.color(255, 85, 85)))
            }
            is ResizePartitionResult.StorageError -> {
                player.sendActionBar(Component.text(localizationProvider.get(
                    player.uniqueId, LocalizationKeys.GENERAL_ERROR))
                    .color(TextColor.color(255, 85, 85)))
            }
        }
    }

    private fun findAdjacentClaims(location: Location): List<Claim> {
        val x = location.blockX.toDouble()
        val y = location.blockY.toDouble()
        val z = location.blockZ.toDouble()
        val world = location.world

        val surroundingLocations = listOf(
            Location(world, x, y, z - 1),
            Location(world, x, y, z + 1),
            Location(world, x - 1, y, z),
            Location(world, x + 1, y, z))
        return surroundingLocations.mapNotNull {
            when (val result = getClaimAtPosition.execute(it.world.uid, it.toPosition2D())) {
                is GetClaimAtPositionResult.Success -> result.claim
                else -> null
            }
        }
    }
}