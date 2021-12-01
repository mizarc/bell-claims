package xyz.mizarc.solidclaims.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Syntax
import org.bukkit.command.CommandSender
import xyz.mizarc.solidclaims.events.ClaimEventHandler
import xyz.mizarc.solidclaims.SolidClaims

class HandleEventsCommand : BaseCommand() {
    @Dependency
    lateinit var plugin : SolidClaims

    @CommandAlias("handle_events")
    @CommandPermission("solidclaims.claim")
    @Syntax("handle_events")
    fun onToggle(sender: CommandSender) {
        ClaimEventHandler.handleEvents = !ClaimEventHandler.handleEvents
        sender.sendMessage("Turned handling: " + ClaimEventHandler.handleEvents)
    }
}