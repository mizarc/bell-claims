package xyz.mizarc.solidclaims.commands

import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.events.ClaimPermission

@CommandAlias("claim")
class UntrustCommand : ClaimCommand() {
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

    @Subcommand("untrust")
    @CommandPermission("solidclaims.command.untrust")
    fun onUntrust(player: Player, otherPlayer: OnlinePlayer, permission: ClaimPermission) {
        val claimPartition = plugin.claimContainer.getClaimPartitionAtLocation(player.location)!!

        val claimPlayers = claimPartition.claim.playerAccesses
        for (claimPlayer in claimPlayers) {
            if (claimPlayer.id == otherPlayer.player.uniqueId) {

                // Check if player doesn't have the permission
                if (permission !in claimPlayer.claimPermissions) {
                    player.sendMessage(
                        "§6${Bukkit.getPlayer(player.uniqueId)?.name} §cdoesn't have permission §6${permission.name}§c.")
                    return
                }

                // Remove permission from player access
                claimPlayer.claimPermissions.remove(permission)
                player.sendMessage(
                    "§6${otherPlayer.player.name} §ahas been revoked the permission §6${permission.name} §afor this claim")
                return
            }
        }

        player.sendMessage(
            "§6${Bukkit.getPlayer(player.uniqueId)?.name} §cdoesn't have permission §6${permission.name}§c.")
        return
    }
}