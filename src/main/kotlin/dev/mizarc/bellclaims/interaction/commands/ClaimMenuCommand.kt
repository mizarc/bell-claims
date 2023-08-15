package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Dependency
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.infrastructure.claims.ClaimRepositorySQLite
import dev.mizarc.bellclaims.interaction.menus.ClaimMenu

@CommandAlias("claimmenu")
class ClaimMenuCommand: BaseCommand() {
    @Dependency
    lateinit var claimRepo: ClaimRepositorySQLite

    @Default
    @CommandPermission("bellclaims.command.claimmenu")
    fun onWarp(player: Player, backCommand: String? = null) {
        ClaimMenu(claimRepo, player).openClaimMenu(backCommand)
    }
}