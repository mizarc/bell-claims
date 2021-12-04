package xyz.mizarc.solidclaims.events

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import xyz.mizarc.solidclaims.claims.Claim
import xyz.mizarc.solidclaims.claims.ClaimContainer
import xyz.mizarc.solidclaims.claims.ClaimPartition
import xyz.mizarc.solidclaims.getClaimTool

/**
 * Actions based on utilising the claim tool.
 * @property claimContainer A reference to the claim containers to modify.
 */
class ClaimToolListener(val claimContainer: ClaimContainer) : Listener {
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
            playerClaimBuilders.add(playerClaimBuilder)
            event.player.sendMessage("New claim building started. First position has been selected.")
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
            claimContainer.getPositionFromLocation(playerClaimBuilder.firstLocation!!),
            claimContainer.getPositionFromLocation(playerClaimBuilder.secondLocation!!))

        // Add to list of claims
        claimContainer.addNewClaim(newClaim)
        claimContainer.addNewClaimPartition(newClaimPartition)
        playerClaimBuilders.remove(playerClaimBuilder)
        event.player.sendMessage("New claim has been created.")
    }

    /**
     * Compares a new claim to the existing claims to see if they overlap.
     * @param playerClaimBuilder
     */
    fun checkValidClaim(playerClaimBuilder: PlayerClaimBuilder) : Boolean {
        val chunks = claimContainer.getClaimChunks(
            claimContainer.getPositionFromLocation(playerClaimBuilder.firstLocation!!),
            claimContainer.getPositionFromLocation(playerClaimBuilder.secondLocation!!))

        val existingPartitions: MutableSet<ClaimPartition> = mutableSetOf()
        for (chunk in chunks) {
            val partitionsAtChunk = claimContainer.getClaimPartitionsAtChunk(chunk) ?: continue
            existingPartitions.addAll(partitionsAtChunk)
        }

        for (partition in existingPartitions) {
            if (partition.isBoxInClaim(claimContainer.getPositionFromLocation(playerClaimBuilder.firstLocation!!),
                    claimContainer.getPositionFromLocation(playerClaimBuilder.secondLocation!!))) {
                return false
            }
        }

        return true
    }
}