package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import dev.mizarc.bellclaims.interaction.menus.MenuNavigator
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.interaction.menus.misc.ClaimListMenu

@CommandAlias("claimmenu")
class ClaimMenuCommand: BaseCommand() {

    @Default
    @CommandPermission("bellclaims.command.claimmenu")
    fun onClaimMenu(player: Player) {
        val menuNavigator = MenuNavigator(player)
        menuNavigator.openMenu(ClaimListMenu(menuNavigator, player))
    }
}