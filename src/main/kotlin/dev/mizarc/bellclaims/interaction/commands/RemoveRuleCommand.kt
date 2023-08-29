package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.interaction.listeners.Flag

@CommandAlias("claim")
class RemoveRuleCommand : ClaimCommand() {

    @Subcommand("removerule")
    @CommandPermission("bellclaims.command.claim.removerule")
    fun onRemoveClaim(player: Player, rule: Flag) {
        val partition = getPartitionAtPlayer(player) ?: return
        val claim = claimService.getById(partition.claimId) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) {
            return
        }

        if (!flagService.doesClaimHaveFlag(claim, rule)) {
            player.sendMessage("§6$rule §cwas not assigned for §6${claim.name}§c.")
            return
        }

        flagService.remove(claim, rule)
        player.sendMessage("§6$rule §aremoved for §6${claim.name}§a.")
    }
}