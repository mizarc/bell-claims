package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import dev.mizarc.bellclaims.application.actions.player.ToggleClaimOverride
import dev.mizarc.bellclaims.application.results.ToggleClaimOverrideResult
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

@CommandAlias("claimoverride")
class ClaimOverrideCommand: BaseCommand(), KoinComponent {
    private val toggleClaimOverride: ToggleClaimOverride by inject()

    @Default
    @CommandPermission("bellclaims.command.claimoverride")
    fun onClaimOverride(player: Player) {
        val result = toggleClaimOverride.execute(player.uniqueId)
        when (result) {
            is ToggleClaimOverrideResult.Success ->  {
                val newState = if (result.isOverrideEnabled) "enabled" else "disabled"
                player.sendMessage("Claim override has been $newState.")
            }
            is ToggleClaimOverrideResult.PlayerNotFound ->
                player.sendMessage("Your player data was not found, contact your administrator for support.")
            is ToggleClaimOverrideResult.StorageError ->
                player.sendMessage("An internal error has occurred, contact your administrator for support.")
        }
    }
}