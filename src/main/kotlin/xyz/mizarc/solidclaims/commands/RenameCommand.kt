package xyz.mizarc.solidclaims.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
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
            player.sendMessage("§cThere is no claim partition at your current location.")
            return true
        }

        // Check if player state exists
        val playerState = plugin.playerContainer.getPlayer(player.uniqueId)
        if (playerState == null) {
            player.sendMessage("§cSomehow, your player data doesn't exist. Please contact an administrator.")
            return true
        }

        if (playerState.claimOverride) {
            return false
        }

        // Check if player owns claim
        if (player.uniqueId != claimPartition.claim.owner.uniqueId) {
            player.sendMessage("§cYou don't have permission to modify this claim.")
            return true
        }

        return false
    }

    @Subcommand("rename")
    @CommandPermission("solidclaims.command.rename")
    fun onRename(player: Player, name: String) {
        if (name.count() > 32) {
            player.sendMessage("§cName must be under 32 characters.")
            return
        }

        val claim = plugin.claimContainer.getClaimPartitionAtLocation(player.location)!!.claim
        claim.name = name
        plugin.database.modifyClaimName(claim.id, name)
        player.sendMessage("§aThe name of the claim has been set to §6$name§a.")
    }
}