package xyz.mizarc.solidclaims.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Dependency
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.PlayerContainer

@CommandAlias("claimoverride")
class ClaimOverrideCommand: BaseCommand() {
    @Dependency
    lateinit var playerContainer: PlayerContainer

    @Default
    @CommandPermission("solidclaims.command.claimoverride")
    fun onClaimOverride(player: Player) {
        val playerState = playerContainer.getPlayer(player.uniqueId)
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