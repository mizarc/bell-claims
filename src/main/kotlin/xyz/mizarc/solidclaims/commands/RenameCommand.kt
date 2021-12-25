package xyz.mizarc.solidclaims.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.PreCommand
import co.aikar.commands.annotation.Subcommand
import org.bukkit.entity.Player

@CommandAlias("claim")
class RenameCommand : ClaimCommand() {

    @PreCommand
    fun preCommand(player: Player): Boolean {
        val claimPartition = plugin.claimContainer.getClaimPartitionAtLocation(player.location)

        // Check if there is a claim at the player's location
        if (claimPartition == null) {
            player.sendMessage("There is no claim partition at your current location.")
            return true
        }

        // Check if player owns claim
        if (player.uniqueId != claimPartition.claim.owner.uniqueId) {
            player.sendMessage("You don't have permission to modify this claim.")
            return true
        }

        return false
    }

    @Subcommand("rename")
    fun onRename(player: Player, name: String) {
        if (name.count() > 32) {
            player.sendMessage("Name must be under 32 characters.")
            return
        }

        val claim = plugin.claimContainer.getClaimPartitionAtLocation(player.location)!!.claim
        claim.name = name
        plugin.database.modifyClaimName(claim.id, name)
        player.sendMessage("The name of the claim has been set to $name")
    }
}