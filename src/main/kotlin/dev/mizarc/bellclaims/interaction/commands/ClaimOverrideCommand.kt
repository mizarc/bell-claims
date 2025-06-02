package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import dev.mizarc.bellclaims.application.actions.player.ToggleClaimOverride
import dev.mizarc.bellclaims.application.results.player.ToggleClaimOverrideResult
import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

@CommandAlias("claimoverride")
class ClaimOverrideCommand: BaseCommand(), KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()
    private val toggleClaimOverride: ToggleClaimOverride by inject()

    @Default
    @CommandPermission("bellclaims.command.claimoverride")
    fun onClaimOverride(player: Player) {
        val result = toggleClaimOverride.execute(player.uniqueId)

        // Execute claim override action and output result to player
        val messageKey = when (result) {
            is ToggleClaimOverrideResult.Success -> {
                if (result.isOverrideEnabled) LocalizationKeys.COMMAND_CLAIM_OVERRIDE_ENABLED
                else LocalizationKeys.COMMAND_CLAIM_OVERRIDE_DISABLED
            }
            is ToggleClaimOverrideResult.PlayerNotFound,
            is ToggleClaimOverrideResult.StorageError -> LocalizationKeys.GENERAL_ERROR
        }
        player.sendMessage(localizationProvider.get(player.uniqueId, messageKey))
    }
}