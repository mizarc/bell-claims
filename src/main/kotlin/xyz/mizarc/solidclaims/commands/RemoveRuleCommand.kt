package xyz.mizarc.solidclaims.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.PreCommand
import co.aikar.commands.annotation.Subcommand
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.events.ClaimRule

@CommandAlias("claim")
class RemoveRuleCommand : ClaimCommand() {
    @PreCommand
    fun preCommand(player: Player): Boolean {
        val claimPartition = plugin.claimContainer.getClaimPartitionAtLocation(player.location)

        if (claimPartition == null) {
            player.sendMessage("There is no claim partition at your current location.")
            return true
        }

        if (player.uniqueId != claimPartition.claim.owner.uniqueId) {
            player.sendMessage("You don't have permission to modify this claim.")
            return true
        }

        return false
    }

    @Subcommand("removerule")
    fun onRemoveClaim(player: Player, rule: ClaimRule) {
        val claimPartition = plugin.claimContainer.getClaimPartitionAtLocation(player.location)!!
        val claim = claimPartition.claim

        if (plugin.claimContainer.removeClaimRule(claim, rule)) {
            player.sendMessage("$rule removed for ${claim.name}.")
            return
        }

        player.sendMessage("$rule was not assigned for ${claim.name}.")
    }
}