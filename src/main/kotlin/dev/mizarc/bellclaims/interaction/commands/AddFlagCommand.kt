package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
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
        if (flagService.getByClaim(claim).contains(rule)) {
            player.sendMessage("§6${claim.name} §calready has §6${rule}§c.")
            return
        }

        flagService.add(claim, rule)
        player.sendMessage("§aAdded §6$rule §afor §6${claim.name}§a.")
    }
}