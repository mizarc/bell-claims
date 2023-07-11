package xyz.mizarc.solidclaims.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Subcommand
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.claims.ClaimRuleRepository
import xyz.mizarc.solidclaims.events.ClaimRule

@CommandAlias("claim")
class AddRuleCommand : ClaimCommand() {

    @Subcommand("addrule")
    @CommandPermission("solidclaims.command.addrule")
    fun onRule(player: Player, rule: ClaimRule) {
        val partition = getPartitionAtPlayer(player) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) {
            return
        }

        val claim = claims.getById(partition.id)!!
        if (claimRuleRepository.doesClaimHaveRule(claim, rule)) {
            player.sendMessage("§6${claim.name} §calready has §6${rule}§c.")
            return
        }

        claimRuleRepository.add(claim, rule)
        player.sendMessage("§aAdded §6$rule §afor §6${claim.name}§a.")
    }
}