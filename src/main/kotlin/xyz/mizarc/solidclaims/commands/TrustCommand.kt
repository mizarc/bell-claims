package xyz.mizarc.solidclaims.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.SolidClaims
import xyz.mizarc.solidclaims.claims.ClaimPlayer
import xyz.mizarc.solidclaims.events.ClaimPermission

@CommandAlias("trust")
class TrustCommand : BaseCommand() {
    @Dependency
    lateinit var plugin : SolidClaims

    @Default
    @CommandCompletion("@players @permissions")
    fun onTrust(player: Player, otherPlayer: Player, permission: String) {
        val claimPartition = plugin.claimContainer.getClaimPartitionAtLocation(player.location)

        // Check if there is a claim at the player's location
        if (claimPartition == null) {
            player.sendMessage("There is no claim at your current location.")
            return
        }

        // Get the second substring in the string because a bug causes the otherPlayer name to come first in permission
        val name = permission.split(' ')[1]

        var perm: ClaimPermission? = null

        for (p in ClaimPermission.values()) {
            if (name == p.alias) {
                perm = p
                break
            }
        }

        if (perm == null) {
            player.sendMessage("$name does not exist!")
            return
        }

        val claimPlayers = claimPartition.claim.claimPlayers
        for (claimPlayer in claimPlayers) {
            if (claimPlayer.id == otherPlayer.uniqueId) {

                // Check if player already has the permission
                if (perm in claimPlayer.claimPermissions) {
                    player.sendMessage(
                        "${Bukkit.getPlayer(player.uniqueId)?.name} already has permission ${perm.name}")
                    return
                }

                // Add new permission to player access
                claimPlayer.claimPermissions.add(perm)
                player.sendMessage("${Bukkit.getPlayer(
                        otherPlayer.uniqueId)?.name} has been given the permission ${perm.name} for this claim")
                return
            }
        }

        // Add new player and permission
        val playerAccess = ClaimPlayer(otherPlayer.uniqueId)
        playerAccess.claimPermissions.add(perm)
        player.sendMessage("${Bukkit.getPlayer(
                otherPlayer.uniqueId)?.name} has been given the permission ${perm.name} for this claim")
    }
}