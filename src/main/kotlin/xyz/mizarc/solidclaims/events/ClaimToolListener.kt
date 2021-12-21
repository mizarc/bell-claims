package xyz.mizarc.solidclaims.events

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NetherWartsState
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import xyz.mizarc.solidclaims.PlayerContainer
import xyz.mizarc.solidclaims.claims.Claim
import xyz.mizarc.solidclaims.claims.ClaimContainer
import xyz.mizarc.solidclaims.claims.ClaimPartition
import xyz.mizarc.solidclaims.getClaimTool

/**
 * Actions based on utilising the claim tool.
 * @property claimContainer A reference to the claim containers to modify.
 */
class ClaimToolListener(val claimContainer: ClaimContainer, val playerContainer: PlayerContainer,
                        val claimVisualiser: ClaimVisualiser) : Listener {
    var playerClaimBuilders: ArrayList<PlayerClaimBuilder> = ArrayList()
    var playerClaimResizers: ArrayList<PlayerClaimResizer> = ArrayList()

    @EventHandler
    fun onUseClaimTool(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.item == null) return
        if (event.item!!.itemMeta != getClaimTool().itemMeta) return

        // Check if player is already making a claim
        lateinit var playerClaimBuilder: PlayerClaimBuilder
        var isMakingClaim = false
        for (player in playerClaimBuilders) {
            if (player.playerId == event.player.uniqueId) {
                isMakingClaim = true
                playerClaimBuilder = player
                break
            }
        }

        // Check if player is already making a resize
        lateinit var playerClaimResizer: PlayerClaimResizer
        var isResizing = false
        for (player in playerClaimResizers) {
            if (player.playerId == event.player.uniqueId) {
                isResizing = true
                playerClaimResizer = player
                break
            }
        }

        // Resize existing claim
        if (isResizing) {
            createNewPartitionArea(event.player, event.clickedBlock!!.location, playerClaimResizer)
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
        if (!checkValidBlock(location)) {
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

        playerClaimBuilders.add(PlayerClaimBuilder(player.uniqueId, location))
        return player.sendMessage("New claim building started. " +
                "You have $remainingClaimBlockCount Blocks and $remainingClaimCount Areas remaining.")
    }

    /**
     * Creates a new claim using a claim builder.
     */
    fun createClaim(player: Player, location: Location, claimBuilder: PlayerClaimBuilder) {
        // Set second location & Check if it overlaps an existing claim
        claimBuilder.secondLocation = location
        if (!checkValidClaim(claimBuilder)) {
            return player.sendMessage("That selection overlaps an existing claim.")
        }

        val remainingClaimBlockCount = playerContainer.getPlayer(player.uniqueId)!!.getRemainingClaimBlockCount()
        val remainingClaimCount = playerContainer.getPlayer(player.uniqueId)!!.getRemainingClaimCount()

        // Check if selection is greater than the player's remaining claim blocks
        if (claimBuilder.getBlockCount()!! > remainingClaimBlockCount) {
            return player.sendMessage("That selection would require an additional " +
                    "${claimBuilder.getBlockCount()!! - remainingClaimCount} claim blocks")
        }

        // Create Claim & Partition
        val newClaim = Claim(location.world!!.uid, Bukkit.getOfflinePlayer(player.uniqueId))
        val newClaimPartition = ClaimPartition(
            newClaim,
            ClaimContainer.getPositionFromLocation(claimBuilder.firstLocation),
            ClaimContainer.getPositionFromLocation(claimBuilder.secondLocation!!))
        newClaim.mainPartition = newClaimPartition
        newClaim.claimPartitions.add(newClaimPartition)

        // Add to list of claims
        playerContainer.getPlayer(player.uniqueId)?.claims?.add(newClaim)
        claimContainer.addNewClaim(newClaim)
        claimContainer.addNewClaimPartition(newClaimPartition)
        playerClaimBuilders.remove(claimBuilder)
        claimVisualiser.updateVisualisation(player, true)
        player.sendMessage("New claim has been created.")
    }

    /**
     * Selects an existing claim corner if it exists.
     */
    fun selectExistingCorner(player: Player, location: Location) : Boolean {
        if (getCornerBlockPartition(location) == null) {
            return false
        }

        playerClaimResizers.add(PlayerClaimResizer(player.uniqueId, getCornerBlockPartition(location)!!,
            Pair(location.x.toInt(), location.z.toInt())))
        player.sendMessage("Claim corner selected. Select a different location to resize the claim.")
        return true
    }

    /**
     * Selects a new position to resize the claim.
     */
    fun createNewPartitionArea(player: Player, location: Location, claimResizer: PlayerClaimResizer) {
        val remainingClaimBlockCount = playerContainer.getPlayer(player.uniqueId)!!.getRemainingClaimBlockCount()

        // Check if claim takes too much space
        if (playerContainer.getPlayer(player.uniqueId)!!.getUsedClaimBlockCount() + claimResizer.extraBlockCount()!! >
                playerContainer.getPlayer(player.uniqueId)!!.getTotalClaimBlockLimit()) {
            return player.sendMessage("That resize would require an additional " +
                    "${claimResizer.extraBlockCount()!! - remainingClaimBlockCount} blocks")
        }

        // Apply the resize
        claimResizer.newLocation = location
        claimContainer.modifyPersistentClaimPartition(claimResizer.claimPartition, claimResizer.setNewCorner())
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

    fun cancelClaimCreation(playerClaimBuilder: PlayerClaimBuilder) {
        playerClaimBuilders.remove(playerClaimBuilder)
    }

    fun cancelClaimResizing(playerClaimResizer: PlayerClaimResizer) {
        playerClaimResizers.remove(playerClaimResizer)
    }

    fun getCornerBlockPartition(location: Location) : ClaimPartition? {
        val chunks = claimContainer.getClaimChunks(
            ClaimContainer.getPositionFromLocation(location),
            ClaimContainer.getPositionFromLocation(location))

        val existingPartitions: MutableSet<ClaimPartition> = mutableSetOf()
        for (chunk in chunks) {
            val partitionsAtChunk = claimContainer.getClaimPartitionsAtChunk(chunk) ?: continue
            existingPartitions.addAll(partitionsAtChunk)
        }

        for (partition in existingPartitions) {
            if (ClaimContainer.getPositionFromLocation(location) in partition.getCornerBlockPositions()) {
                return partition
            }
        }

        return null
    }

    /**
     *
     */
    fun checkValidBlock(location: Location) : Boolean {
        val chunks = claimContainer.getClaimChunks(
            ClaimContainer.getPositionFromLocation(location),
            ClaimContainer.getPositionFromLocation(location))

        val existingPartitions: MutableSet<ClaimPartition> = mutableSetOf()
        for (chunk in chunks) {
            val partitionsAtChunk = claimContainer.getClaimPartitionsAtChunk(chunk) ?: continue
            existingPartitions.addAll(partitionsAtChunk)
        }

        for (partition in existingPartitions) {
            if (partition.isLocationInClaim(location)) {
                return false
            }
        }

        return true
    }

    /**
     * Compares a new claim to the existing claims to see if they overlap.
     * @param playerClaimBuilder
     */
    fun checkValidClaim(playerClaimBuilder: PlayerClaimBuilder) : Boolean {
        val chunks = claimContainer.getClaimChunks(
            ClaimContainer.getPositionFromLocation(playerClaimBuilder.firstLocation),
            ClaimContainer.getPositionFromLocation(playerClaimBuilder.secondLocation!!))

        val existingPartitions: MutableSet<ClaimPartition> = mutableSetOf()
        for (chunk in chunks) {
            val partitionsAtChunk = claimContainer.getClaimPartitionsAtChunk(chunk) ?: continue
            existingPartitions.addAll(partitionsAtChunk)
        }

        for (partition in existingPartitions) {
            if (partition.isBoxInClaim(ClaimContainer.getPositionFromLocation(playerClaimBuilder.firstLocation),
                    ClaimContainer.getPositionFromLocation(playerClaimBuilder.secondLocation!!))) {
                return false
            }
        }

        return true
    }

    /**
     * Gets the PlayerClaimBuilder object of the player if they are making a claim.
     * @param player The player object to check.
     * @return The PlayerClaimBuilder object of the player.
     */
    fun getPlayerMakingClaim(player: Player) : PlayerClaimBuilder? {
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
    fun getPlayerResizingClaim(player: Player) : PlayerClaimResizer? {
        for (resizerPlayer in playerClaimResizers) {
            if (resizerPlayer.playerId == player.uniqueId) {
                return resizerPlayer
            }
        }
        return null
    }
}