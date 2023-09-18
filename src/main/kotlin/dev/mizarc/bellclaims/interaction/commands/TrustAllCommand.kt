package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import dev.mizarc.bellclaims.api.enums.DefaultPermissionChangeResult
import dev.mizarc.bellclaims.api.enums.PlayerPermissionChangeResult
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.domain.permissions.ClaimPermission

@CommandAlias("claim")
class TrustAllCommand : ClaimCommand() {
    @Subcommand("trustall")
    @CommandPermission("bellclaims.command.claim.trustall")
    fun onTrustAll(player: Player, permission: ClaimPermission) {
        val partition = getPartitionAtPlayer(player) ?: return
        val claim = claimService.getById(partition.claimId) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) {
            return
        }

        // Add permission for player and output result
        when (defaultPermissionService.add(claim, permission)) {
            DefaultPermissionChangeResult.UNCHANGED ->
                player.sendMessage("§6${Bukkit.getPlayer(player.uniqueId)?.name} §calready has " +
                        "permission §6${permission.name}§c.")
            DefaultPermissionChangeResult.SUCCESS ->
                player.sendMessage("§aAll players now have §6${permission.name} permissions §afor this claim.")
            else -> player.sendMessage("Unknown Error.")
        }
    }
}