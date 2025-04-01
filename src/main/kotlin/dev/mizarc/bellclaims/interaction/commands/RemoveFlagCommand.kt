package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import dev.mizarc.bellclaims.application.enums.FlagChangeResult
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.domain.flags.Flag

@CommandAlias("claim")
class RemoveFlagCommand : ClaimCommand() {
    @Subcommand("removeflag")
    @CommandPermission("bellclaims.command.claim.removeflag")
    fun onRemoveFlag(player: Player, flag: Flag) {
        val partition = getPartitionAtPlayer(player) ?: return
        val claim = claimService.getById(partition.claimId) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) {
            return
        }

        when (flagService.remove(claim, flag)) {
            FlagChangeResult.UNCHANGED ->
                player.sendMessage("Claim §6${claim.name}§c does not have §6$flag §cenabled.")
            FlagChangeResult.SUCCESS ->
                player.sendMessage("§6$flag §adisabled for claim §6${claim.name}§a.")
            else -> player.sendMessage("Unknown Error.")
        }
    }
}