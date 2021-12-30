package xyz.mizarc.solidclaims.events

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import xyz.mizarc.solidclaims.PlayerContainer
import xyz.mizarc.solidclaims.claims.Position
import xyz.mizarc.solidclaims.claims.Claim
import xyz.mizarc.solidclaims.claims.ClaimContainer
import xyz.mizarc.solidclaims.claims.Partition
import xyz.mizarc.solidclaims.getClaimTool
import java.time.Instant

/**
 * Actions based on utilising the claim tool.
 * @property claimContainer A reference to the claim containers to modify.
 */
class ClaimToolListener(val claimContainer: ClaimContainer, val playerContainer: PlayerContainer,
                        val claimVisualiser: ClaimVisualiser) : Listener {
    var playerClaimBuilders: ArrayList<AreaBuilder> = ArrayList()
    var partitionResizeBuilders: ArrayList<PartitionResizeBuilder> = ArrayList()

    @EventHandler
    fun onUseClaimTool(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.item == null) return
        if (event.item!!.itemMeta != getClaimTool().itemMeta) return

        // Check if player is already making a claim
        lateinit var playerClaimBuilder: AreaBuilder
        var isMakingClaim = false
        for (player in playerClaimBuilders) {
            if (player.playerId == event.player.uniqueId) {
                isMakingClaim = true
                playerClaimBuilder = player
                break
            }
        }

        // Check if player is already making a resize
        lateinit var partitionResizeBuilder: PartitionResizeBuilder
        var isResizing = false
        for (player in partitionResizeBuilders) {
            if (player.playerId == event.player.uniqueId) {
                isResizing = true
                partitionResizeBuilder = player
                break
            }
        }

        // Resize existing claim
        if (isResizing) {
            createNewPartitionArea(event.player, event.clickedBlock!!.location, partitionResizeBuilder)
            return
        }

        // Make new claim
        if (isMakingClaim) {
            createClaim(event.player, event.clickedBlock!!.location, playerClaimBuilder)
            return
        }

        // Select corner of existing claim
        if (selectExistingCorner(event.player, event.clickedBlock!!.location)) {
            return
        }

        selectFirstLocation(event.player, event.clickedBlock!!.location)
    }

    /**
     * Selects the corner of the claim that is going to be resized.
     */
    fun selectFirstLocation(player: Player, location: Location) {
        // Check if the selected spot exists in an existing claim.
        if (claimContainer.isPositionOverlap(Position(location), location.world!!)) {
            player.sendMessage("That spot is in an existing claim.")
            return
        }

        val remainingClaimBlockCount = playerContainer.getPlayer(player.uniqueId)!!.getRemainingClaimBlockCount()
        val remainingClaimCount = playerContainer.getPlayer(player.uniqueId)!!.getRemainingClaimCount()

        // Check if the player has already hit the claim limit.
        if (remainingClaimCount < 1) {
            return player.sendMessage("You have already hit your claim limit. Try removing an existing claim.")
        }

        // Check if the player already hit claim block limit.
        if (remainingClaimBlockCount < 1) {
            return player.sendMessage("You have already hit your claim block limit. " +
                    "Try removing or resizing an existing claim.")
        }

        playerClaimBuilders.add(AreaBuilder(player.uniqueId, Position(location.x.toInt(), location.z.toInt())))
        return player.sendMessage("New claim building started. " +
                "You have $remainingClaimBlockCount Blocks and $remainingClaimCount Areas remaining.")
    }

    /**
     * Creates a new claim using a claim builder.
     */
    fun createClaim(player: Player, location: Location, claimBuilder: AreaBuilder) {
        claimBuilder.secondPosition = Position(location.x.toInt(), location.z.toInt())
        val area = claimBuilder.build() ?: return
        // Set second location & Check if it overlaps an existing claim
        if (claimContainer.isAreaOverlap(area, location.world!!)) {
            return player.sendMessage("That selection overlaps an existing claim.")
        }

        if (area.getXLength() < 5 || area.getZLength() < 5) {
            return player.sendMessage("The claim must be at least 5x5 blocks.")
        }

        val remainingClaimBlockCount = playerContainer.getPlayer(player.uniqueId)!!.getRemainingClaimBlockCount()
        val remainingClaimCount = playerContainer.getPlayer(player.uniqueId)!!.getRemainingClaimCount()

        // Check if selection is greater than the player's remaining claim blocks
        if (area.getBlockCount() > remainingClaimBlockCount) {
            return player.sendMessage("That selection would require an additional " +
                    "${area.getBlockCount() - remainingClaimCount} claim blocks")
        }

        val adjacentPartition = claimContainer.getPartitionAdjacent(area, location.world!!)
        if (adjacentPartition != null) {
            appendPartitionToClaim(player, claimBuilder, adjacentPartition.claim)
            return
        }

        // Create Claim & Partition
        val newClaim = Claim(location.world!!.uid, Bukkit.getOfflinePlayer(player.uniqueId), Instant.now())
        val newPartition = Partition(newClaim, area)
        newClaim.mainPartition = newPartition

        // Add to list of claims
        playerContainer.getPlayer(player.uniqueId)?.claims?.add(newClaim)
        claimContainer.addNewClaim(newClaim)
        claimContainer.addNewClaimPartition(newPartition)
        playerClaimBuilders.remove(claimBuilder)
        claimVisualiser.updateVisualisation(player, true)
        player.sendMessage("New claim has been created.")
    }

    fun appendPartitionToClaim(player: Player, claimBuilder: AreaBuilder, claim: Claim) {
        val area = claimBuilder.build() ?: return
        val newPartition = Partition(claim, area)
        claimContainer.addNewClaimPartition(newPartition)
        playerClaimBuilders.remove(claimBuilder)
        claimVisualiser.updateVisualisation(player, true)
        val name = if (claim.name != null) claim.name else claim.id.toString().substring(0, 7)
        player.sendMessage("New claim partition has been added to $name.")
    }

    /**
     * Selects an existing claim corner if it exists.
     */
    fun selectExistingCorner(player: Player, location: Location) : Boolean {
        val partition = claimContainer.getCornerPartition(Position(location), location.world!!) ?: return false

        // Check if player state exists
        val playerState = playerContainer.getPlayer(player.uniqueId)
        if (playerState == null) {
            player.sendMessage("Somehow, your player data doesn't exist. Please contact an administrator.")
            return true
        }

        // Check for permission to modify claim.
        if (playerState.claimOverride) {
            val pass: Unit = Unit
        }
        else if (partition.claim.owner.uniqueId != player.uniqueId) {
            player.sendMessage("You don't have permission to modify that claim.")
            return false
        }

        partitionResizeBuilders.add(PartitionResizeBuilder(player.uniqueId, partition, Position(location)))
        player.sendMessage("Claim corner selected. Select a different location to resize the claim.")
        return true
    }

    /**
     * Selects a new position to resize the claim.
     */
    fun createNewPartitionArea(player: Player, location: Location, claimResizer: PartitionResizeBuilder) {
        claimResizer.setNewCorner(Position(location.x.toInt(), location.z.toInt()))
        val newPartition = claimResizer.build()

        // Check if selection overlaps an existing claim
        if (!claimContainer.isPartitionOverlap(newPartition)) {
            return player.sendMessage("That selection overlaps an existing claim.")
        }

        // Check if claim meets minimum size
        if (newPartition.area.getXLength() < 5 || newPartition.area.getZLength() < 5) {
            return player.sendMessage("The claim must be at least 5x5 blocks.")
        }

        // Check if claim takes too much space
        val remainingClaimBlockCount = playerContainer.getPlayer(player.uniqueId)!!.getRemainingClaimBlockCount()
        if (playerContainer.getPlayer(player.uniqueId)!!.getUsedClaimBlockCount() + claimResizer.extraBlockCount()!! >
                playerContainer.getPlayer(player.uniqueId)!!.getTotalClaimBlockLimit()) {
            return player.sendMessage("That resize would require an additional " +
                    "${claimResizer.extraBlockCount()!! - remainingClaimBlockCount} blocks")
        }

        // Check if partition is the main
        claimContainer.removeClaimPartition(claimResizer.partition)
        if (claimResizer.partition.claim.isPartitionMain(claimResizer.partition)) {
            claimResizer.partition.claim.mainPartition = newPartition
        }
        // Check if claim resize would result in this claim being disconnected from the main
        else if (!claimResizer.partition.claim.isPartitionConnectedToMain(newPartition)) {
            claimContainer.addClaimPartition(claimResizer.partition)
            return player.sendMessage(
                "That resize would result in this partition being disconnected from the main partition.")
        }

        // Check if claim resize would result in any claim islands
        claimContainer.addClaimPartition(newPartition)
        if (claimResizer.partition.claim.isAnyDisconnectedPartitions()) {
            if (claimResizer.partition.claim.isPartitionMain(newPartition)) {
                claimResizer.partition.claim.mainPartition = claimResizer.partition
            }
            claimContainer.removeClaimPartition(newPartition)
            claimContainer.addClaimPartition(claimResizer.partition)
            return player.sendMessage(
                "That resize would result in an unconnected partition island."
            )
        }

        if (claimResizer.partition.claim.isPartitionMain(claimResizer.partition)) {
            claimContainer.modifyMainPartition(claimResizer.partition.claim, newPartition)
        }

        // Apply the resize
        claimContainer.removeClaimPartition(claimResizer.build())
        claimContainer.modifyPersistentClaimPartition(claimResizer.partition, newPartition)
        partitionResizeBuilders.remove(claimResizer)
        claimVisualiser.oldPartitions.add(claimResizer.partition)
        claimVisualiser.unrenderOldClaims(player)
        claimVisualiser.oldPartitions.clear()
        claimVisualiser.updateVisualisation(player, true)
        return player.sendMessage("Claim corner resized.")
    }

    @EventHandler
    fun onToolSwitch(event: PlayerItemHeldEvent) {
        if (event.player.inventory.getItem(event.previousSlot) != getClaimTool()) {
            return
        }

        // Cancel claim building
        val playerClaimBuilder = getPlayerMakingClaim(event.player)
        if (playerClaimBuilder != null) {
            cancelClaimCreation(playerClaimBuilder)
            event.player.sendMessage("Claim tool unequipped. Claim building has been cancelled.")
            return
        }

        // Cancel claim resizing
        val playerClaimResizer = getPlayerResizingClaim(event.player)
        if (playerClaimResizer != null) {
            cancelClaimResizing(playerClaimResizer)
            event.player.sendMessage("Claim tool unequipped. Claim resizing has been cancelled.")
        }
    }

    fun cancelClaimCreation(playerClaimBuilder: AreaBuilder) {
        playerClaimBuilders.remove(playerClaimBuilder)
    }

    fun cancelClaimResizing(partitionResizeBuilder: PartitionResizeBuilder) {
        partitionResizeBuilders.remove(partitionResizeBuilder)
    }

    /**
     * Gets the PlayerClaimBuilder object of the player if they are making a claim.
     * @param player The player object to check.
     * @return The PlayerClaimBuilder object of the player.
     */
    fun getPlayerMakingClaim(player: Player) : AreaBuilder? {
        for (builderPlayer in playerClaimBuilders) {
            if (builderPlayer.playerId == player.uniqueId) {
                return builderPlayer
            }
        }
        return null
    }

    /**
     * Gets the PlayerClaimBuilder object of the player if they are making a claim.
     * @param player The player object to check.
     * @return The PlayerClaimBuilder object of the player.
     */
    fun getPlayerResizingClaim(player: Player) : PartitionResizeBuilder? {
        for (resizerPlayer in partitionResizeBuilders) {
            if (resizerPlayer.playerId == player.uniqueId) {
                return resizerPlayer
            }
        }
        return null
    }
}