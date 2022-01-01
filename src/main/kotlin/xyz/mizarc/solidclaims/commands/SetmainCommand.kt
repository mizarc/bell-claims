package xyz.mizarc.solidclaims.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.PreCommand
import co.aikar.commands.annotation.Subcommand
import org.bukkit.entity.Player

@CommandAlias("claim")
class SetmainCommand: ClaimCommand() {
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

    @Subcommand("setmain")
    @CommandPermission("solidclaims.command.setmain")
    fun onSetmain(player: Player) {
        val claimPartition = plugin.claimContainer.getClaimPartitionAtLocation(player.location) ?: return
        plugin.claimContainer.modifyMainPartition(claimPartition.claim, claimPartition)
        player.sendMessage("§aThis partition has now been set as the main.")
    }
}