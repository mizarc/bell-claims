package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import dev.mizarc.bellclaims.api.enums.DefaultPermissionChangeResult
import dev.mizarc.bellclaims.api.enums.PlayerPermissionChangeResult
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.domain.permissions.ClaimPermission

@CommandAlias("claim")
class UntrustAllCommand : ClaimCommand() {
    @Subcommand("untrust")
    @CommandPermission("bellclaims.command.claim.untrustall")
    fun onUntrustAll(player: Player, claimPermission: ClaimPermission) {
        val partition = getPartitionAtPlayer(player) ?: return
        val claim = claimService.getById(partition.claimId) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) {
            return
        }

        // Remove permission for player and output result
        when (defaultPermissionService.remove(claim, claimPermission)) {
            DefaultPermissionChangeResult.UNCHANGED ->
                player.sendMessage("§cClaim doesn't have §6${claimPermission.name} §cset as a default permission.")
            DefaultPermissionChangeResult.SUCCESS ->
                player.sendMessage("§aPlayers no longer have §6${claimPermission.name} §apermissions for this claim.")
            else ->
                player.sendMessage("Unknown Error.")
        }
    }
}