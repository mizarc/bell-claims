package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import dev.mizarc.bellclaims.api.enums.FlagChangeResult
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.domain.flags.Flag

@CommandAlias("claim")
class AddFlagCommand : ClaimCommand() {

    @Subcommand("addflag")
    @CommandPermission("bellclaims.command.claim.addflag")
    fun onFlag(player: Player, rule: Flag) {
        val partition = getPartitionAtPlayer(player) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) {
            return
        }

        val claim = claimService.getById(partition.claimId) ?: return
        when (flagService.add(claim, rule)) {
            FlagChangeResult.UNCHANGED ->
                player.sendMessage("§6${claim.name} §calready has §6${rule}§c.")
            FlagChangeResult.SUCCESS ->
                player.sendMessage("§aAdded §6$rule §afor §6${claim.name}§a.")
            else -> player.sendMessage("Unknown Error.")
        }

    }
}