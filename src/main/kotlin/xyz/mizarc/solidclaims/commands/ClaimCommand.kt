package xyz.mizarc.solidclaims.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Syntax
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.SolidClaims
import xyz.mizarc.solidclaims.getClaimTool

class ClaimCommand : BaseCommand() {
    @Dependency
    lateinit var plugin : SolidClaims

    @CommandAlias("claim")
    @CommandPermission("solidclaims.claim")
    @Syntax("claim")
    fun onClaim(sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage("This command can only be run by a player.")
        }

        val player: Player = sender as Player
        sender.sendMessage("You have been given the claim tool")
        player.inventory.addItem(getClaimTool())
    }
}