package xyz.mizarc.solidclaims.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.PreCommand
import co.aikar.commands.annotation.Subcommand
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.events.ClaimRule

@CommandAlias("claim")
class AddRuleCommand : ClaimCommand() {
    @PreCommand
    fun preCommand(player: Player): Boolean {
        val claimPartition = plugin.claimContainer.getClaimPartitionAtLocation(player.location)

        if (claimPartition == null) {
            player.sendMessage("There is no claim partition at your current location.")
            return true
        }

        // Check if player state exists
        val playerState = plugin.playerContainer.getPlayer(player.uniqueId)
        if (playerState == null) {
            player.sendMessage("Somehow, your player data doesn't exist. Please contact an administrator.")
            return true
        }

        if (playerState.claimOverride) {
            return false
        }

        if (player.uniqueId != claimPartition.claim.owner.uniqueId) {
            player.sendMessage("You don't have permission to modify this claim.")
            return true
        }

        return false
    }

    @Subcommand("addrule")
    fun onRule(player: Player, rule: ClaimRule) {
        val claim = plugin.claimContainer.getClaimPartitionAtLocation(player.location)!!.claim

        if (plugin.claimContainer.addNewClaimRule(claim, rule)) {
            player.sendMessage("Added $rule for ${claim.name}")
            return
        }

        player.sendMessage("${claim.name} already has ${rule}!")
    }
}