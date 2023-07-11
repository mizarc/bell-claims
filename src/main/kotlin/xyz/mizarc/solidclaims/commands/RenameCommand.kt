package xyz.mizarc.solidclaims.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.PreCommand
import co.aikar.commands.annotation.Subcommand
import org.bukkit.entity.Player

@CommandAlias("claim")
class RenameCommand : ClaimCommand() {

    @Subcommand("rename")
    @CommandPermission("solidclaims.command.rename")
    fun onRename(player: Player, name: String) {
        val partition = getPartitionAtPlayer(player) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) {
            return
        }

        // Ensure name is under character limit
        if (name.count() > 32) {
            player.sendMessage("§cName must be under 32 characters.")
            return
        }

        // Set new name
        val claim = claims.getById(partition.claimId)!!
        claim.name = name
        claims.update(claim)
        player.sendMessage("§aThe name of the claim has been set to §6$name§a.")
    }
}