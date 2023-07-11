package xyz.mizarc.solidclaims.events

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import xyz.mizarc.solidclaims.ClaimQuery
import xyz.mizarc.solidclaims.players.PlayerStateRepository
import xyz.mizarc.solidclaims.claims.ClaimRepository
import xyz.mizarc.solidclaims.getClaimTool
import xyz.mizarc.solidclaims.partitions.*

/**
 * Actions based on utilising the claim tool.
 * @property claimContainer A reference to the claim containers to modify.
 */
class ClaimToolListener(val claims: ClaimRepository, val partitions: PartitionRepository,
                        val playerStates: PlayerStateRepository, val claimQuery: ClaimQuery,
                        val claimVisualiser: ClaimVisualiser) : Listener {
    private var partitionBuilders = mutableMapOf<Player, Partition.Builder>()
    private var partitionResizers = mutableMapOf<Player, Partition.Resizer>()

    @EventHandler
    fun onUseClaimTool(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.item == null) return
        if (event.item!!.itemMeta != getClaimTool().itemMeta) return

        // Resizes an existing partition
        val partitionResizer = partitionResizers[event.player]
        if (partitionResizer != null) {
            resizePartition(event.player, event.clickedBlock!!.location, partitionResizer)
            return
        }

        // Creates a new partition
        val partitionBuilder = partitionBuilders[event.player]
        if (partitionBuilder != null) {
            createClaim(event.player, event.clickedBlock!!.location, partitionBuilder)
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
            event.player.sendMessage("§cClaim tool unequipped. Claim building has been cancelled.")
            return
        }

        // Cancel claim resizing
        val partitionResizer = partitionResizers[event.player]
        if (partitionResizer != null) {
            partitionResizers.remove(event.player)
            event.player.sendMessage("§cClaim tool unequipped. Claim resizing has been cancelled.")
        }
    }

    /**
     * Selects the corner of the claim that is going to be resized.
     */
    fun selectNewCorner(player: Player, location: Location) {
        // Check if the selected spot exists in an existing claim.
        if (claimQuery.isLocationOverlap(location)) {
            player.sendMessage("§cThat spot is in an existing claim.")
            return
        }

        val remainingClaimBlockCount = claimQuery.getRemainingClaimBlockCount(player) ?: return
        val remainingClaimCount = claimQuery.getRemainingClaimCount(player) ?: return

        // Check if the player has already hit the claim limit.
        if (remainingClaimCount < 1) {
            return player.sendMessage("§cYou have already hit your claim limit. Try removing an existing claim.")
        }

        // Check if the player already hit claim block limit.
        if (remainingClaimBlockCount < 1) {
            return player.sendMessage("§cYou have already hit your claim block limit. " +
                    "Try removing or resizing an existing claim.")
        }

        partitionBuilders[player] = Partition.Builder(Position(location))
        return player.sendMessage("§aNew claim building started. " +
                "You have §6$remainingClaimBlockCount §aBlocks and §6$remainingClaimCount §aAreas remaining.")
    }

    /**
     * Creates a new claim using a claim builder.
     */
    fun createClaim(player: Player, location: Location, partitionBuilder: Partition.Builder) {
        partitionBuilder.secondPosition = Position(location.x.toInt(), location.z.toInt())
        val partition = partitionBuilder.build()

        val result = claimQuery.addPartition(player, partition, location.world!!.uid)
        when (result) {
            ClaimQuery.PartitionCreationResult.Overlap ->
                player.sendMessage("§cThat selection overlaps an existing claim.")
            ClaimQuery.PartitionCreationResult.TooSmall ->
                player.sendMessage("§cThe claim must be at least 5x5 blocks.")
            ClaimQuery.PartitionCreationResult.InsufficientClaims -> TODO()
            ClaimQuery.PartitionCreationResult.InsufficientBlocks -> player.sendMessage("§cThat selection would require an additional " +
                "§6${partition.area.getBlockCount() - claimQuery.getRemainingClaimBlockCount(player)!!} §cclaim blocks.")
            ClaimQuery.PartitionCreationResult.SuccessfulExisting ->
                player.sendMessage("§aNew claim partition has been added to §6${claims.getById(partition.claimId)!!.name}.")
            ClaimQuery.PartitionCreationResult.SuccessfulNew ->
                player.sendMessage("§aNew claim has been created.")
        }

        // Update builders list and visualisation
        partitionBuilders.remove(player)
        claimVisualiser.updateVisualisation(player, true)
    }

    /**
     * Selects an existing claim corner if one is selected that a player has access to.
     */
    fun selectExistingCorner(player: Player, location: Location) : Boolean {
        val partition = claimQuery.getByLocation(location) ?: return false
        val claim = claims.getById(partition.claimId) ?: return false

        // Check if player state exists
        val playerState = playerStates.get(player)
        if (playerState == null) {
            player.sendMessage("§cSomehow, your player data doesn't exist. Please contact an administrator.")
            return false
        }

        // Check for permission to modify claim.
        if (playerState.claimOverride) {}
        else if (claim.owner.uniqueId != player.uniqueId) {
            player.sendMessage("§cYou don't have permission to modify that claim.")
            return false
        }

        if (!partition.isPositionInCorner(Position(location))) {
            return false
        }

        partitionResizers[player] = Partition.Resizer(partition, Position(location))
        player.sendMessage("§aClaim corner selected. Select a different location to resize the claim.")
        return true
    }

    /**
     * Selects a new position to resize the claim.
     */
    fun resizePartition(player: Player, location: Location, partitionResizer: Partition.Resizer) {
        partitionResizer.setNewCorner(Position(location.x.toInt(), location.z.toInt()))

        val result = claimQuery.resizePartition(player,
            partitionResizer.partition, WorldArea(partitionResizer.newArea, location.world!!.uid))
        when (result) {
            ClaimQuery.PartitionResizeResult.Overlap ->
                player.sendMessage("§cThat selection overlaps an existing claim.")
            ClaimQuery.PartitionResizeResult.TooSmall ->
                player.sendMessage("§cThe claim must be at least 5x5 blocks.")
            ClaimQuery.PartitionResizeResult.InsufficientBlocks ->
                player.sendMessage("§cThat resize would require an additional " +
                        "§6${partitionResizer.getExtraBlockCount() - claimQuery.getRemainingClaimBlockCount(player)!!} §cblocks.")
            ClaimQuery.PartitionResizeResult.Successful -> player.sendMessage("§aClaim partition successfully resized.")
        }

        // Update visualiser
        claimVisualiser.oldPartitions.add(partitionResizer.partition)
        claimVisualiser.unrenderOldClaims(player)
        claimVisualiser.oldPartitions.clear()
        claimVisualiser.updateVisualisation(player, true)
        partitionResizers.remove(player)
    }
}