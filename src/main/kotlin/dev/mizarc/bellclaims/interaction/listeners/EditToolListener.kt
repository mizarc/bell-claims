package dev.mizarc.bellclaims.interaction.listeners

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
import dev.mizarc.bellclaims.application.services.ClaimService
import dev.mizarc.bellclaims.application.services.PartitionService
import dev.mizarc.bellclaims.application.services.PlayerLimitService
import dev.mizarc.bellclaims.application.services.PlayerStateService
import dev.mizarc.bellclaims.application.enums.PartitionCreationResult
import dev.mizarc.bellclaims.application.enums.PartitionResizeResult
import dev.mizarc.bellclaims.application.events.PartitionModificationEvent
import dev.mizarc.bellclaims.domain.claims.ClaimRepository
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.infrastructure.getClaimTool
import dev.mizarc.bellclaims.domain.partitions.Position2D
import dev.mizarc.bellclaims.interaction.menus.EditToolMenu
import dev.mizarc.bellclaims.domain.partitions.Partition
import dev.mizarc.bellclaims.infrastructure.persistence.Config
import dev.mizarc.bellclaims.interaction.visualisation.Visualiser

import dev.mizarc.bellclaims.utils.getLangText

/**
 * Actions based on utilising the claim tool.
 * @property claimContainer A reference to the claim containers to modify.
 */
class EditToolListener(private val claims: ClaimRepository,
                       private val partitionService: PartitionService,
                       private val playerLimitService: PlayerLimitService,
                       private val playerStateService: PlayerStateService,
                       private val claimService: ClaimService,
                       private val visualiser: Visualiser,
                       private val config: Config) : Listener {
    private var partitionBuilders = mutableMapOf<Player, Partition.Builder>()
    private var partitionResizers = mutableMapOf<Player, Partition.Resizer>()

    @EventHandler
    fun onUseClaimTool(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val item = event.item ?: return
        val clickedBlock = event.clickedBlock ?: return
        if (item.itemMeta != getClaimTool().itemMeta) return

        // Open menu if in offhand
        if (event.hand == EquipmentSlot.OFF_HAND) {
            val location = event.clickedBlock?.location ?: return
            val partition: Partition? = partitionService.getByLocation(location)
            EditToolMenu(claimService, partitionService, playerStateService, event.player, visualiser, partition)
                .openEditToolMenu()
            return
        }

        visualiser.refresh(event.player)

        // Resizes an existing partition
        val partitionResizer = partitionResizers[event.player]
        if (partitionResizer != null) {
            resizePartition(event.player, clickedBlock.location, partitionResizer)
            return
        }

        // Creates a new partition
        val partitionBuilder = partitionBuilders[event.player]
        if (partitionBuilder != null) {
            createPartition(event.player, clickedBlock.location, partitionBuilder)
            return
        }

        // Select corner of existing claim
        if (selectExistingCorner(event.player, clickedBlock.location)) {
            return
        }

        // Selects a fresh location to start a new claim
        selectNewCorner(event.player, clickedBlock.location)
    }

    @EventHandler
    fun onToolSwitch(event: PlayerItemHeldEvent) {
        if (event.player.inventory.getItem(event.previousSlot) != getClaimTool()) return

        // Cancel claim building on unequip
        val partitionBuilder = partitionBuilders[event.player]
        if (partitionBuilder != null) {
            partitionBuilders.remove(event.player)
            event.player.sendActionBar(
                Component.text(getLangText("ClaimToolUnequipped"))
                .color(TextColor.color(255, 85, 85)))
            return
        }

        // Cancel claim resizing
        val partitionResizer = partitionResizers[event.player]
        if (partitionResizer != null) {
            partitionResizers.remove(event.player)
            event.player.sendActionBar(
                Component.text(getLangText("ClaimToolUnequippedResizingCancelled"))
                    .color(TextColor.color(255, 85, 85)))
        }
    }

    /**
     * Selects the corner of the claim that is going to be resized.
     */
    fun selectNewCorner(player: Player, location: Location) {
        // Check if the selected spot exists in an existing claim
        if (partitionService.getByLocation(location) != null) {
            player.sendActionBar(
                Component.text("That spot is inside an existing claim")
                    .color(TextColor.color(255, 85, 85)))
            return
        }

        // Find claims next to the current selection
        var selectedClaim: Claim? = null
        val adjacentPartitions = findAdjacentPartitions(location)
        for (partition in adjacentPartitions) {
            val claim = claimService.getById(partition.claimId) ?: continue
            if (claim.owner.uniqueId == player.uniqueId) {
                selectedClaim = claim
                break
            }
        }

        // Check if selection exists next to any of the player's owned claims
        if (selectedClaim == null) {
            return player.sendActionBar(
                Component.text(getLangText("InvalidSpotForClaim"))
                    .color(TextColor.color(255, 85, 85)))
        }

        val remainingClaimBlockCount = playerLimitService.getRemainingClaimBlockCount(player)

        // Check if the player already hit claim block limit
        if (remainingClaimBlockCount < 1) {
            return player.sendActionBar(
                Component.text(getLangText("ClaimBlockLimitReached"))
                    .color(TextColor.color(255, 85, 85)))
        }

        // Start partition building
        partitionBuilders[player] = Partition.Builder(selectedClaim.id, Position2D(location))
        return player.sendActionBar(
            Component.text(getLangText("NewClaimExtensionStarted1") + "$remainingClaimBlockCount" + getLangText("NewClaimExtensionStarted2"))
                .color(TextColor.color(85, 255, 85)))
    }

    /**
     * Creates a new partition using a claim builder.
     */
    fun createPartition(player: Player, location: Location, partitionBuilder: Partition.Builder) {
        partitionBuilder.secondPosition2D = Position2D(location.x.toInt(), location.z.toInt())
        val partition = partitionBuilder.build()
        val claim = claimService.getById(partition.claimId) ?: return
        when (partitionService.append(partition.area, claim)) {
            PartitionCreationResult.OVERLAP ->
                return player.sendActionBar(Component.text("That selection overlaps an existing partition")
                    .color(TextColor.color(255, 85, 85)))
            PartitionCreationResult.TOO_CLOSE ->
                return player.sendActionBar(Component.text("That selection is too close to another claim")
                    .color(TextColor.color(255, 85, 85)))
            PartitionCreationResult.TOO_SMALL ->
                return player.sendActionBar(Component.text("The selection must be at least " +
                        "${config.minimumPartitionSize}x${config.minimumPartitionSize} blocks")
                    .color(TextColor.color(255, 85, 85)))
            PartitionCreationResult.INSUFFICIENT_BLOCKS ->
                return player.sendActionBar(Component.text("That selection would require an additional " +
                        "${partition.area.getBlockCount() - playerLimitService.getRemainingClaimBlockCount(player)} " +
                        "claim blocks.")
                    .color(TextColor.color(255, 85, 85)))
            PartitionCreationResult.SUCCESS ->
                player.sendActionBar(Component.text("New partition has been added to " + claim.name)
                    .color(TextColor.color(85, 255, 85)))
            PartitionCreationResult.NOT_CONNECTED -> player.sendActionBar(Component.text("That selection is " +
                    "not connected to your claim.")
                .color(TextColor.color(255, 85, 85)))
        }

        // Update builders list and visualisation
        partitionBuilders.remove(player)
        val event = PartitionModificationEvent(partition)
        event.callEvent()
    }

    /**
     * Selects an existing claim corner if one is selected that a player has access to.
     */
    fun selectExistingCorner(player: Player, location: Location) : Boolean {
        val partition = partitionService.getByLocation(location) ?: return false
        val claim = claims.getById(partition.claimId) ?: return false

        // Check if player state exists
        val playerState = playerStateService.getByPlayer(player)
        if (playerState == null) {
            player.sendPlainMessage("§cSomehow, your player data doesn't exist. Please contact an administrator.")
            return false
        }

        // Check for permission to modify claim.
        if (playerState.claimOverride) {}
        else if (claim.owner.uniqueId != player.uniqueId) {
            player.sendActionBar(
                Component.text("You don't have permission to modify that claim.")
                    .color(TextColor.color(255, 85, 85)))
            return false
        }

        if (!partition.isPositionInCorner(Position2D(location))) {
            return false
        }

        partitionResizers[player] = Partition.Resizer(partition, Position2D(location))
        val remainingClaimBlockCount = playerLimitService.getRemainingClaimBlockCount(player)
        player.sendActionBar(
            Component.text("Claim corner selected. Select a different location to resize. " +
                    "You have $remainingClaimBlockCount blocks remaining.")
                .color(TextColor.color(85, 255, 85)))
        return true
    }

    /**
     * Selects a new position to resize the claim.
     */
    fun resizePartition(player: Player, location: Location, partitionResizer: Partition.Resizer) {
        partitionResizer.setNewCorner(Position2D(location.x.toInt(), location.z.toInt()))

        val remainingClaimBlockCount = playerLimitService.getRemainingClaimBlockCount(player)
        val newPartition = partitionResizer.partition.copy()
        newPartition.area = partitionResizer.newArea
        val result = partitionService.resize(partitionResizer.partition, partitionResizer.newArea)
        val newRemainingClaimBlockCount = playerLimitService.getRemainingClaimBlockCount(player)
        when (result) {
            PartitionResizeResult.OVERLAP ->
                player.sendActionBar(Component.text("That selection overlaps an existing claim")
                        .color(TextColor.color(255, 85, 85)))
            PartitionResizeResult.TOO_CLOSE ->
                player.sendActionBar(Component.text("That selection is too close to another claim")
                    .color(TextColor.color(255, 85, 85)))
            PartitionResizeResult.EXPOSED_CLAIM_HUB ->
                player.sendActionBar(Component.text("That selection would result in the claim bell " +
                        "being outside the claim area")
                    .color(TextColor.color(255, 85, 85)))
            PartitionResizeResult.TOO_SMALL ->
                player.sendActionBar(Component.text("The selection must be at least " +
                        "${config.minimumPartitionSize}x${config.minimumPartitionSize} blocks")
                    .color(TextColor.color(255, 85, 85)))
            PartitionResizeResult.DISCONNECTED ->
                player.sendActionBar(Component.text("Resizing to that size would result in a gap between " +
                        "claim partitions")
                    .color(TextColor.color(255, 85, 85)))
            PartitionResizeResult.INSUFFICIENT_BLOCKS ->
                player.sendActionBar(Component.text("That resize would require an additional " +
                        "${partitionResizer.getExtraBlockCount() - remainingClaimBlockCount} blocks")
                    .color(TextColor.color(255, 85, 85)))
            PartitionResizeResult.SUCCESS ->
                player.sendActionBar(
                    Component.text("Claim partition successfully resized. " +
                            "You have $newRemainingClaimBlockCount blocks remaining.")
                        .color(TextColor.color(85, 255, 85)))
        }

        // Update visualiser
        if (result == PartitionResizeResult.SUCCESS) {
            claimService.getById(newPartition.claimId) ?: return
            val event = PartitionModificationEvent(newPartition)
            event.callEvent()
            partitionResizers.remove(player)
        }
    }

    private fun findAdjacentPartitions(location: Location): List<Partition> {
        val x = location.blockX.toDouble()
        val y = location.blockY.toDouble()
        val z = location.blockZ.toDouble()
        val world = location.world

        val surroundingLocations = listOf(
            Location(world, x, y, z - 1),
            Location(world, x, y, z + 1),
            Location(world, x - 1, y, z),
            Location(world, x + 1, y, z))
        return surroundingLocations.mapNotNull { partitionService.getByLocation(it) }
    }
}