package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.interaction.listeners.ClaimPermission

@CommandAlias("claim")
class TrustCommand : ClaimCommand() {
    @Subcommand("trust")
    @CommandPermission("bellclaims.command.claim.trust")
    fun onTrust(player: Player, otherPlayer: OnlinePlayer, permission: ClaimPermission) {
        val partition = getPartitionAtPlayer(player) ?: return
        val claim = claimService.getById(partition.claimId) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) {
            return
        }

        if (playerPermissionService.doesPlayerHavePermission(claim, player, permission)) {
            player.sendMessage(
                "§6${Bukkit.getPlayer(player.uniqueId)?.name} §calready has permission §6${permission.name}§c.")
            return
        }

        playerPermissionService.addForPlayer(claim, otherPlayer.player, permission)
        player.sendMessage(
            "§6${Bukkit.getPlayer(otherPlayer.player.uniqueId)?.name} " +
            "§ahas been given the permission §6${permission.name} §afor this claim.")
    }
}