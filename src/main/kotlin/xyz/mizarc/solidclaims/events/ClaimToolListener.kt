package xyz.mizarc.solidclaims.events

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import xyz.mizarc.solidclaims.claims.ClaimContainer
import xyz.mizarc.solidclaims.claims.ClaimPartition
import xyz.mizarc.solidclaims.getClaimTool

class ClaimToolListener(val claimContainer: ClaimContainer) : Listener {
    var playerClaimBuilders: ArrayList<PlayerClaimBuilder> = ArrayList()

    @EventHandler
    fun onUseClaimTool(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.item == null) return
        if (event.item!!.itemMeta != getClaimTool().itemMeta) return

        // Check if player is already making a claim
        var isMakingClaim = false
        for (player in playerClaimBuilders) {
            if (player.playerId == event.player.uniqueId) {
                isMakingClaim = true
                break
            }
        }

        // Set first location
        val playerClaimBuilder = PlayerClaimBuilder(event.player.uniqueId)
        if (!isMakingClaim) {
            playerClaimBuilder.firstLocation = event.clickedBlock?.location!!
            event.player.sendMessage("New claim building started. First position has been selected.")
            return
        }

        // Set second location
        playerClaimBuilder.secondLocation = event.clickedBlock?.location!!
        checkValidClaim(playerClaimBuilder)
    }

    fun checkValidClaim(playerClaimBuilder: PlayerClaimBuilder) {
        val chunks = claimContainer.getClaimChunks(
            claimContainer.getPositionFromLocation(playerClaimBuilder.firstLocation!!),
            claimContainer.getPositionFromLocation(playerClaimBuilder.secondLocation!!))

        val partitionsInClaim: MutableSet<ClaimPartition> = mutableSetOf()
        for(chunk in chunks) {
            partitionsInClaim.addAll(claimContainer.getClaimPartitionsAtChunk(chunk)!!)
        }


        /*val newClaim: ClaimPartition = ClaimPartition(
            claimContainer.getPositionFromLocation(playerClaimBuilder.firstLocation),
            claimContainer.getPositionFromLocation(playerClaimBuilder.secondLocation))
        claimContainer.getClaimPartitionsAtChunk()*/
    }
}