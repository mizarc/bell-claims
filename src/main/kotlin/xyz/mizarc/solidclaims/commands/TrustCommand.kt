package xyz.mizarc.solidclaims.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.SolidClaims
import xyz.mizarc.solidclaims.claims.PlayerAccess
import xyz.mizarc.solidclaims.events.ClaimPermission

@CommandAlias("claim")
class TrustCommand : ClaimCommand() {
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

    @Subcommand("trust")
    @CommandPermission("solidclaims.command.trust")
    fun onTrust(player: Player, otherPlayer: OnlinePlayer, permission: ClaimPermission) {
        val claim = plugin.claimContainer.getClaimPartitionAtLocation(player.location)!!.claim

        //val claimPlayers = claimPartition.claim.playerAccesses
        if (plugin.claimContainer.addNewClaimPermission(claim, otherPlayer.player, permission)) {
                player.sendMessage("§6${Bukkit.getPlayer(
                    otherPlayer.player.uniqueId)?.name} §ahas been given the permission §6${permission.name} §afor this claim.")
                return
        }

        player.sendMessage(
            "§6${Bukkit.getPlayer(player.uniqueId)?.name} §calready has permission §6${permission.name}§c.")
        return
    }
}