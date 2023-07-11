package xyz.mizarc.solidclaims.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.PreCommand
import co.aikar.commands.annotation.Subcommand
import org.bukkit.entity.Player

@CommandAlias("claim")
class SetmainCommand: ClaimCommand() {

    @Subcommand("setmain")
    @CommandPermission("solidclaims.command.setmain")
    fun onSetmain(player: Player) {
        val partition = getPartitionAtPlayer(player) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) {
            return
        }

        // Check if main partition is already set as this one
        val claim = claims.getById(partition.claimId)!!
        if (claim.mainPartitionId == partition.id) {
            player.sendMessage("§cThis partition is already set as the main.")
            return
        }

        // Set main partition
        claim.mainPartitionId = partition.id
        claims.update(claim)
        player.sendMessage("§aThis partition has now been set as the main.")
    }
}