package xyz.mizarc.solidclaims.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.SolidClaims
import xyz.mizarc.solidclaims.claims.ClaimPlayer
import xyz.mizarc.solidclaims.events.ClaimPermission

@CommandAlias("trust")
class UntrustCommand {
    @Dependency
    lateinit var plugin : SolidClaims

    @Default
    fun onUntrust(player: Player, otherPlayer: OnlinePlayer, permission: ClaimPermission) {
        val claimPartition = plugin.claimContainer.getClaimPartitionAtLocation(player.location)

        // Check if there is a claim at the player's location
        if (claimPartition == null) {
            player.sendMessage("There is no claim at your current location.")
            return
        }

        val claimPlayers = claimPartition.claim.claimPlayers
        for (claimPlayer in claimPlayers) {
            if (claimPlayer.id == otherPlayer.player.uniqueId) {

                // Check if player already has the permission
                if (permission !in claimPlayer.claimPermissions) {
                    player.sendMessage(
                        "${Bukkit.getPlayer(player.uniqueId)?.name} doesn't have permission ${permission.name}")
                    return
                }

                // Remove permission from player access
                claimPlayer.claimPermissions.remove(permission)
                player.sendMessage(
                    "${otherPlayer.player.name} has been revoked the permission ${permission.name} for this claim")
                return
            }
        }
    }
}