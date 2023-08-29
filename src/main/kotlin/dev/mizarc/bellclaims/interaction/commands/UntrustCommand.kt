package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.interaction.listeners.ClaimPermission

@CommandAlias("claim")
class UntrustCommand : ClaimCommand() {

    @Subcommand("untrust")
    @CommandPermission("bellclaims.command.claim.untrust")
    fun onUntrust(player: Player, otherPlayer: OnlinePlayer, claimPermission: ClaimPermission) {
        val partition = getPartitionAtPlayer(player) ?: return
        val claim = claimService.getById(partition.claimId) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) {
            return
        }

        // Alert if permission isn't found
        if (playerPermissionService.doesPlayerHavePermission(claim, player, claimPermission)) {
            player.sendMessage("§6${otherPlayer.player.name} §cdoesn't have permission §6${claimPermission.name}§c.")
            return
        }

        playerPermissionService.removeForPlayer(claim, player, claimPermission)
        player.sendMessage("§6${otherPlayer.player.name} §ahas been revoked " +
            "the permission §6${claimPermission.name} §afor this claim")
    }
}