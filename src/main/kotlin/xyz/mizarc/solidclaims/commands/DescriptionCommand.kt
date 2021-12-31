package xyz.mizarc.solidclaims.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.PreCommand
import co.aikar.commands.annotation.Subcommand
import org.bukkit.entity.Player

@CommandAlias("claim")
class DescriptionCommand : ClaimCommand() {

    @PreCommand
    fun preCommand(player: Player): Boolean {
        val claimPartition = plugin.claimContainer.getClaimPartitionAtLocation(player.location)

        // Check if there is a claim at the player's location
        if (claimPartition == null) {
            player.sendMessage("There is no claim partition at your current location.")
            return true
        }

        // Check if player state exists
        val playerState = plugin.playerContainer.getPlayer(player.uniqueId)
        if (playerState == null) {
            player.sendMessage("Somehow, your player data doesn't exist. Please contact an administrator.")
            return true
        }

        if (playerState.claimOverride) {
            return false
        }

        // Check if player owns claim
        if (player.uniqueId != claimPartition.claim.owner.uniqueId) {
            player.sendMessage("You don't have permission to modify this claim.")
            return true
        }

        return false
    }

    @Subcommand("description")
    fun onDescription(player: Player, description: String) {
        val claim = plugin.claimContainer.getClaimPartitionAtLocation(player.location)!!.claim
        claim.description = description
        plugin.database.modifyClaimDescription(claim.id, description)
        player.sendMessage("New claim description has been set.")
    }
}