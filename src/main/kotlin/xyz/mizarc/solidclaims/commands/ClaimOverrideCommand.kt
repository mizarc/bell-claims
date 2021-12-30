package xyz.mizarc.solidclaims.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Dependency
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.PlayerContainer

@CommandAlias("claimoverride")
class ClaimOverrideCommand: BaseCommand() {
    @Dependency
    lateinit var playerContainer: PlayerContainer

    @Default
    fun onClaimOverride(player: Player) {
        val playerState = playerContainer.getPlayer(player.uniqueId)
        if (playerState == null) {
            player.sendMessage("Somehow, your player data doesn't exist. Please contact an administrator.")
            return
        }

        if (playerState.claimOverride) {
            playerState.claimOverride = false
            player.sendMessage("You are no longer overriding claim permissions.")
            return
        }

        playerState.claimOverride = true
        player.sendMessage("You are now overriding claim permissions.")
    }
}