package xyz.mizarc.solidclaims.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.SolidClaims
import xyz.mizarc.solidclaims.claims.PlayerAccess
import xyz.mizarc.solidclaims.events.ClaimPermission

@CommandAlias("trust")
class TrustCommand : BaseCommand() {
    @Dependency
    lateinit var plugin : SolidClaims

    @Default
    @CommandCompletion("@players @permissions")
    fun onTrust(player: Player, otherPlayer: OnlinePlayer, permission: ClaimPermission) {
        val claimPartition = plugin.claimContainer.getClaimPartitionAtLocation(player.location)

        // Check if there is a claim at the player's location
        if (claimPartition == null) {
            player.sendMessage("There is no claim at your current location.")
            return
        }

        val claimPlayers = claimPartition.claim.playerAccesses
        for (claimPlayer in claimPlayers) {
            if (claimPlayer.id == otherPlayer.player.uniqueId) {

                // Check if player already has the permission
                if (permission in claimPlayer.claimPermissions) {
                    player.sendMessage(
                        "${Bukkit.getPlayer(player.uniqueId)?.name} already has permission ${permission.name}")
                    return
                }

                // Add new permission to player access
                claimPlayer.claimPermissions.add(permission)
                player.sendMessage(
                    "${otherPlayer.player.name} has been given the permission ${permission.name} for this claim")
                return
            }
        }

        // Add new player and permission
        val playerAccess = PlayerAccess(otherPlayer.player.uniqueId)
        playerAccess.claimPermissions.add(permission)
        claimPartition.claim.playerAccesses.add(playerAccess)
        player.sendMessage("${Bukkit.getPlayer(
                otherPlayer.player.uniqueId)?.name} has been given the permission ${permission.name} for this claim")
    }
}