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
            player.sendMessage("§cThere is no claim partition at your current location.")
            return true
        }

        // Check if player state exists
        val playerState = plugin.playerContainer.getPlayer(player.uniqueId)
        if (playerState == null) {
            player.sendMessage("§cSomehow, your player data doesn't exist. Please contact an administrator.")
            return true
        }

        if (playerState.claimOverride) {
            return false
        }

        if (player.uniqueId != claimPartition.claim.owner.uniqueId) {
            player.sendMessage("§cYou don't have permission to modify this claim.")
            return true
        }

        return false
    }

    @Subcommand("removerule")
    fun onRemoveClaim(player: Player, rule: ClaimRule) {
        val claimPartition = plugin.claimContainer.getClaimPartitionAtLocation(player.location)!!
        val claim = claimPartition.claim

        if (plugin.claimContainer.removeClaimRule(claim, rule)) {
            player.sendMessage("§6$rule §aremoved for §6${claim.name}§a.")
            return
        }

        player.sendMessage("§6$rule §cwas not assigned for §6${claim.name}§c.")
    }
}