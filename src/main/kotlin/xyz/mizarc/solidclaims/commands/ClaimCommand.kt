package xyz.mizarc.solidclaims.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.BukkitCommandIssuer
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Syntax
import org.bukkit.command.CommandSender
import xyz.mizarc.solidclaims.SolidClaims

class ClaimCommand : BaseCommand() {
    @Dependency
    lateinit var plugin : SolidClaims

    @CommandAlias("claim")
    @CommandPermission("solidclaims.claim")
    @Syntax("claim")
    fun onClaim(sender: CommandSender) {
        val issuer: BukkitCommandIssuer = plugin.commandManager.getCommandIssuer(sender)
        issuer.sendMessage("wack")
    }
}