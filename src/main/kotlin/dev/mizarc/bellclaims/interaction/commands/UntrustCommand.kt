package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import dev.mizarc.bellclaims.application.enums.PlayerPermissionChangeResult
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.domain.entities.ClaimPermission

@CommandAlias("claim")
class UntrustCommand : ClaimCommand() {
    @Subcommand("untrust")
    @CommandPermission("bellclaims.command.claim.untrust")
    fun onUntrust(player: Player, targetPlayer: OnlinePlayer, claimPermission: ClaimPermission) {
        val partition = getPartitionAtPlayer(player) ?: return
        val claim = claimService.getById(partition.claimId) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) {
            return
        }

        // Remove permission for player and output result
        when (playerPermissionService.removeForPlayer(claim, targetPlayer.player, claimPermission)) {
            PlayerPermissionChangeResult.UNCHANGED ->
                player.sendMessage("§6${targetPlayer.player.name} §cdoesn't have permission " +
                        "§6${claimPermission.name}§c.")
            PlayerPermissionChangeResult.SUCCESS ->
                player.sendMessage("§6${targetPlayer.player.name} §ahas been revoked permission " +
                        "§6${claimPermission.name} §afor this claim.")
            else ->
                player.sendMessage("Unknown Error.")
        }
    }
}