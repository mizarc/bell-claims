package dev.mizarc.bellclaims.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.listeners.ClaimRule

@CommandAlias("claim")
class RemoveRuleCommand : ClaimCommand() {

    @Subcommand("removerule")
    @CommandPermission("solidclaims.command.removerule")
    fun onRemoveClaim(player: Player, rule: ClaimRule) {
        val partition = getPartitionAtPlayer(player) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) {
            return
        }

        val claim = claims.getById(partition.claimId)!!
        if (!claimRuleRepository.doesClaimHaveRule(claim, rule)) {
            player.sendMessage("§6$rule §cwas not assigned for §6${claim.name}§c.")
            return
        }

        claimRuleRepository.remove(claim, rule)
        player.sendMessage("§6$rule §aremoved for §6${claim.name}§a.")
    }
}