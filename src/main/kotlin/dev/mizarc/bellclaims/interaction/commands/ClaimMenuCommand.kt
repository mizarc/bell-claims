package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Dependency
import dev.mizarc.bellclaims.application.services.ClaimService
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.interaction.menus.ClaimListMenu

@CommandAlias("claimmenu")
class ClaimMenuCommand: BaseCommand() {
    @Dependency
    lateinit var claimService: ClaimService

    @Default
    @CommandPermission("bellclaims.command.claimmenu")
    fun onClaimMenu(player: Player, backCommand: String? = null) {
        ClaimListMenu(claimService, player).openClaimListMenu(backCommand)
    }
}