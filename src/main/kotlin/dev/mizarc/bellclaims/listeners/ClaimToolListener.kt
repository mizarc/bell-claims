package dev.mizarc.bellclaims.listeners

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
import dev.mizarc.bellclaims.ClaimService
import dev.mizarc.bellclaims.infrastructure.PartitionService
import dev.mizarc.bellclaims.api.claims.ClaimRepository
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.infrastructure.players.PlayerStateRepository
import dev.mizarc.bellclaims.infrastructure.getClaimTool
import dev.mizarc.bellclaims.domain.partitions.Position2D
import dev.mizarc.bellclaims.menus.EditToolMenu
import dev.mizarc.bellclaims.domain.partitions.Partition
import dev.mizarc.bellclaims.partitions.*
import java.util.*

/**
 * Actions based on utilising the claim tool.
 * @property claimContainer A reference to the claim containers to modify.
 */
class ClaimToolListener(val claims: ClaimRepository, val playerStates: PlayerStateRepository,
                        val claimService: ClaimService, val partitionService: PartitionService,
                        val claimVisualiser: ClaimVisualiser
) : Listener {
    private var partitionBuilders = mutableMapOf<Player, Partition.Builder>()
    private var partitionResizers = mutableMapOf<Player, Partition.Resizer>()

    @EventHandler
    fun onUseClaimTool(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.item == null) return
        if (event.item!!.itemMeta != getClaimTool().itemMeta) return

        // Open menu if in offhand
        if (event.hand == EquipmentSlot.OFF_HAND) {
            val location = event.clickedBlock?.location ?: return
            val partition: Partition? = partitionService.getByLocation(location)
            EditToolMenu(event.player, claimService, partitionService, playerStates, claimVisualiser,
                partition).openEditToolMenu()
            return
        }

        claimVisualiser.refreshVisualisation(event.player)

        // Resizes an existing partition
        val partitionResizer = partitionResizers[event.player]
        if (partitionResizer != null) {
            resizePartition(event.player, event.clickedBlock!!.location, partitionResizer)
            return
        }

        // Creates a new partition
        val partitionBuilder = partitionBuilders[event.player]
        if (partitionBuilder != null) {
            createPartition(event.player, event.clickedBlock!!.location, partitionBuilder)
            return
        }

        // Select corner of existing claim
        if (selectExistingCorner(event.player, event.clickedBlock!!.location)) {
            return
        }

        // Selects a fresh location to start a new claim
        selectNewCorner(event.player, event.clickedBlock!!.location)
    }

    @EventHandler
    fun onToolSwitch(event: PlayerItemHeldEvent) {
        if (event.player.inventory.getItem(event.previousSlot) != getClaimTool()) return

        // Cancel claim building on unequip
        val partitionBuilder = partitionBuilders[event.player]
        if (partitionBuilder != null) {
            partitionBuilders.remove(event.player)
            event.player.sendActionBar(
                Component.text("Claim tool unequipped. Claim building has been cancelled.")
                .color(TextColor.color(255, 85, 85)))
            return
        }

        // Cancel claim resizing
        val partitionResizer = partitionResizers[event.player]
        if (partitionResizer != null) {
            partitionResizers.remove(event.player)
            event.player.sendActionBar(
                Component.text("Claim tool unequipped. Claim resizing has been cancelled.")
                    .color(TextColor.color(255, 85, 85)))
        }
    }

    /**
     * Selects the corner of the claim that is going to be resized.
     */
    fun selectNewCorner(player: Player, location: Location) {
        // Check if the selected spot exists in an existing claim
        if (partitionService.isLocationOverlap(location)) {
            player.sendActionBar(
                Component.text("That spot is inside an existing claim")
                    .color(TextColor.color(255, 85, 85)))
            return
        }

        // Find claims next to the current selection
        var selectedClaim: Claim? = null
        val adjacentPartitions = partitionService.getAdjacent(location)
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
                Component.text("That spot is neither next to nor a corner of any existing claim")
                    .color(TextColor.color(255, 85, 85)))
        }

        val remainingClaimBlockCount = claimService.getRemainingClaimBlockCount(player) ?: return

        // Check if the player already hit claim block limit
        if (remainingClaimBlockCount < 1) {
            return player.sendActionBar(
                Component.text("You have already hit your claim block limit")
                    .color(TextColor.color(255, 85, 85)))
        }

        // Start partition building
        partitionBuilders[player] = Partition.Builder(selectedClaim.id, Position2D(location))
        return player.sendActionBar(
            Component.text("New claim extension started. " +
                    "You have $remainingClaimBlockCount blocks remaining")
                .color(TextColor.color(85, 255, 85)))
    }

    /**
     * Creates a new partition using a claim builder.
     */
    fun createPartition(player: Player, location: Location, partitionBuilder: Partition.Builder) {
        partitionBuilder.secondPosition2D = Position2D(location.x.toInt(), location.z.toInt())
        val partition = partitionBuilder.build()

        val result = partitionService.addPartition(player, partition, location.world!!.uid)
        when (result) {
            PartitionService.PartitionCreationResult.Overlap ->
                player.sendActionBar(Component.text("That selection overlaps an existing partition")
                    .color(TextColor.color(255, 85, 85)))
            PartitionService.PartitionCreationResult.TooClose ->
                player.sendActionBar(Component.text("That selection is too close to another claim")
                    .color(TextColor.color(255, 85, 85)))
            PartitionService.PartitionCreationResult.TooSmall ->
                player.sendActionBar(Component.text("The claim must be at least 5x5 blocks")
                    .color(TextColor.color(255, 85, 85)))
            PartitionService.PartitionCreationResult.InsufficientBlocks ->
                player.sendActionBar(Component.text("That selection would require an additional " +
                        "${partition.area.getBlockCount() - claimService.getRemainingClaimBlockCount(player)!!} " +
                        "claim blocks.")
                    .color(TextColor.color(255, 85, 85)))
            PartitionService.PartitionCreationResult.Successful ->
                player.sendActionBar(Component.text("New partition has been added to " +
                        claims.getById(partition.claimId)!!.name)
                    .color(TextColor.color(255, 85, 85)))


            PartitionService.PartitionCreationResult.NotConnected -> TODO()
        }

        // Update builders list and visualisation
        if (result == PartitionService.PartitionCreationResult.Successful) {
            partitionBuilders.remove(player)
            val claim = claimService.getById(partitionBuilder.claimId) ?: return
            claimVisualiser.registerClaimUpdate(claim)
        }
    }

    /**
     * Selects an existing claim corner if one is selected that a player has access to.
     */
    fun selectExistingCorner(player: Player, location: Location) : Boolean {
        val partition = partitionService.getByLocation(location) ?: return false
        val claim = claims.getById(partition.claimId) ?: return false

        // Check if player state exists
        val playerState = playerStates.get(player)
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
        player.sendActionBar(
            Component.text("Claim corner selected. Select a different location to resize the claim.")
                .color(TextColor.color(85, 255, 85)))
        return true
    }

    /**
     * Selects a new position to resize the claim.
     */
    fun resizePartition(player: Player, location: Location, partitionResizer: Partition.Resizer) {
        partitionResizer.setNewCorner(Position2D(location.x.toInt(), location.z.toInt()))

        val remainingClaimBLockCount = claimService.getRemainingClaimBlockCount(player)
        if (remainingClaimBLockCount == null) {
            player.sendPlainMessage("§cSomehow, your player data doesn't exist. Please contact an administrator.")
            return
        }

        val newPartition = partitionResizer.partition.copy()
        newPartition.area = partitionResizer.newArea
        val result = partitionService.resizePartition(player, newPartition)
        when (result) {
            PartitionService.PartitionResizeResult.Overlap ->
                player.sendActionBar(Component.text("That selection overlaps an existing claim")
                        .color(TextColor.color(255, 85, 85)))
            PartitionService.PartitionResizeResult.TooClose ->
                player.sendActionBar(Component.text("That selection is too close to another claim")
                    .color(TextColor.color(255, 85, 85)))
            PartitionService.PartitionResizeResult.ExposedClaimHub ->
                player.sendActionBar(Component.text("That selection would result in the claim bell " +
                        "being outside the claim area")
                    .color(TextColor.color(255, 85, 85)))
            PartitionService.PartitionResizeResult.TooSmall ->
                player.sendActionBar(Component.text("The claim must be at least 5x5 blocks")
                    .color(TextColor.color(255, 85, 85)))
            PartitionService.PartitionResizeResult.DisconnectedPartition ->
                player.sendActionBar(Component.text("Resizing to that size would result in a gap between " +
                        "claim partitions")
                    .color(TextColor.color(255, 85, 85)))
            PartitionService.PartitionResizeResult.InsufficientBlocks ->
                player.sendActionBar(Component.text("That resize would require an additional " +
                        "${partitionResizer.getExtraBlockCount() - remainingClaimBLockCount} blocks")
                    .color(TextColor.color(255, 85, 85)))
            PartitionService.PartitionResizeResult.Successful ->
                player.sendActionBar(
                    Component.text("Claim partition successfully resized")
                        .color(TextColor.color(85, 255, 85)))
        }

        // Update visualiser
        if (result == PartitionService.PartitionResizeResult.Successful) {
            val claim = claimService.getById(newPartition.claimId) ?: return
            claimVisualiser.registerClaimUpdate(claim)
            partitionResizers.remove(player)
        }
    }
}