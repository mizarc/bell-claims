package xyz.mizarc.solidclaims.events

import org.bukkit.Bukkit
import org.bukkit.Location
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
class ClaimToolListener(val claimContainer: ClaimContainer, val playerContainer: PlayerContainer) : Listener {
    var playerClaimBuilders: ArrayList<PlayerClaimBuilder> = ArrayList()

    @EventHandler
    fun onUseClaimTool(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.item == null) return
        if (event.item!!.itemMeta != getClaimTool().itemMeta) return

        // Check if player is already making a claim
        var playerClaimBuilder = PlayerClaimBuilder(event.player.uniqueId)
        var isMakingClaim = false
        for (player in playerClaimBuilders) {
            if (player.playerId == event.player.uniqueId) {
                isMakingClaim = true
                playerClaimBuilder = player
                break
            }
        }

        // Set first location
        if (!isMakingClaim) {
            playerClaimBuilder.firstLocation = event.clickedBlock?.location
            if (!checkValidBlock(event.clickedBlock?.location!!)) {
                event.player.sendMessage("That spot is in an existing claim.")
                return
            }

            playerClaimBuilders.add(playerClaimBuilder)
            val remainingClaims = playerContainer.getPlayer(event.player.uniqueId)!!.getTotalClaimLimit() -
                    playerContainer.getPlayer(event.player.uniqueId)!!.getUsedClaimCount()
            val remainingClaimBlocks = playerContainer.getPlayer(event.player.uniqueId)!!.getTotalClaimBlockLimit() -
                    playerContainer.getPlayer(event.player.uniqueId)!!.getUsedClaimBlockCount()
            event.player.sendMessage("New claim building started. " +
                    "You have $remainingClaimBlocks Blocks and $remainingClaims Areas remaining.")
            return
        }

        // Set second location & Check if it overlaps an existing claim
        playerClaimBuilder.secondLocation = event.clickedBlock?.location
        if (!checkValidClaim(playerClaimBuilder)) {
            event.player.sendMessage("That selection overlaps an existing claim.")
            return
        }

        // Create Claim & Partition
        val newClaim = Claim(event.clickedBlock!!.world.uid, Bukkit.getOfflinePlayer(event.player.uniqueId))
        val newClaimPartition = ClaimPartition(
            newClaim,
            ClaimContainer.getPositionFromLocation(playerClaimBuilder.firstLocation!!),
            ClaimContainer.getPositionFromLocation(playerClaimBuilder.secondLocation!!))
        newClaim.mainPartition = newClaimPartition

        // Add to list of claims
        playerContainer.getPlayer(event.player.uniqueId)?.claims?.add(newClaim)
        claimContainer.addNewClaim(newClaim)
        claimContainer.addNewClaimPartition(newClaimPartition)
        playerClaimBuilders.remove(playerClaimBuilder)
        event.player.sendMessage("New claim has been created.")
    }

    @EventHandler
    fun onToolSwitch(event: PlayerItemHeldEvent) {
        if (event.player.inventory.getItem(event.previousSlot) != getClaimTool()) {
            return
        }

        val playerClaimBuilder = getPlayerMakingClaim(event.player)
        if (playerClaimBuilder != null) {
            cancelClaimCreation(playerClaimBuilder)
            event.player.sendMessage("Claim tool unequipped. Claim building has been cancelled.")
        }
    }

    fun cancelClaimCreation(playerClaimBuilder: PlayerClaimBuilder) {
        playerClaimBuilders.remove(playerClaimBuilder)
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
            ClaimContainer.getPositionFromLocation(playerClaimBuilder.firstLocation!!),
            ClaimContainer.getPositionFromLocation(playerClaimBuilder.secondLocation!!))

        val existingPartitions: MutableSet<ClaimPartition> = mutableSetOf()
        for (chunk in chunks) {
            val partitionsAtChunk = claimContainer.getClaimPartitionsAtChunk(chunk) ?: continue
            existingPartitions.addAll(partitionsAtChunk)
        }

        for (partition in existingPartitions) {
            if (partition.isBoxInClaim(ClaimContainer.getPositionFromLocation(playerClaimBuilder.firstLocation!!),
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
}