package dev.mizarc.bellclaims.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Dependency
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.claims.ClaimRepository
import dev.mizarc.bellclaims.menus.ClaimMenu

@CommandAlias("claimmenu")
class ClaimMenuCommand: BaseCommand() {
    @Dependency
    lateinit var claimRepo: ClaimRepository

    @Default
    fun onWarp(player: Player, backCommand: String? = null) {
        ClaimMenu(claimRepo, player).openClaimMenu(backCommand)
    }
}