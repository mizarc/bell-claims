package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Dependency
import dev.mizarc.bellclaims.api.PlayerStateService
import dev.mizarc.bellclaims.domain.players.PlayerStateRepository
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.infrastructure.persistence.players.PlayerStateRepositoryMemory

@CommandAlias("claimoverride")
class ClaimOverrideCommand: BaseCommand() {
    @Dependency
    lateinit var playerState: PlayerStateService

    @Default
    @CommandPermission("bellclaims.command.claimoverride")
    fun onClaimOverride(player: Player) {
        val playerState = playerState.getByPlayer(player)
        if (playerState == null) {
            player.sendMessage("§cSomehow, your player data doesn't exist. Please contact an administrator.")
            return
        }

        if (playerState.claimOverride) {
            playerState.claimOverride = false
            player.sendMessage("§aYou are no longer overriding claim permissions.")
            return
        }

        playerState.claimOverride = true
        player.sendMessage("§aYou are now overriding claim permissions.")
    }
}