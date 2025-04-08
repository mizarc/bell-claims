package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import dev.mizarc.bellclaims.application.actions.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.UpdateClaimName
import dev.mizarc.bellclaims.application.results.TextValidationErrorResult
import dev.mizarc.bellclaims.application.results.UpdateClaimAttributeResult
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

@CommandAlias("claim")
class RenameCommand : ClaimCommand(), KoinComponent {
    private val updateClaimName: UpdateClaimName by inject()
    private val getClaimDetails: GetClaimDetails by inject()

    @Subcommand("description")
    @CommandPermission("bellclaims.command.claim.description")
    fun onRename(player: Player, name: String) {
        // Gets the partition at the player's current location
        val partition = getPartitionAtPlayer(player) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) return

        // Update name and notify player of result
        val result = updateClaimName.execute(partition.claimId, name)
        when (result) {
            is UpdateClaimAttributeResult.Success -> {
                val claimName = getClaimDetails.execute(partition.claimId)?.name ?: "(Could not retrieve name)"
                player.sendMessage("§aClaim $claimName has been renamed to $name.")
            }
            is UpdateClaimAttributeResult.ClaimNotFound -> {
                player.sendMessage("Claim was not found.")
            }
            is UpdateClaimAttributeResult.InputTextInvalid -> {
                result.errors.forEach { error ->
                    when (error) {
                        is TextValidationErrorResult.ExceededCharacterLimit ->
                            player.sendMessage("Name of ${name.count()} characters exceeds character " +
                                    "limit of ${error.maxCharacters}.")
                        is TextValidationErrorResult.InvalidCharacters ->
                            player.sendMessage("Name contains invalid characters: ${error.invalidCharacters}")
                        is TextValidationErrorResult.ContainsBlacklistedWord ->
                            player.sendMessage("Name contains a blacklisted word: ${error.blacklistedWord}")
                        else ->
                            player.sendMessage("Name contains an unknown error.")
                    }
                }
            }
            is UpdateClaimAttributeResult.StorageError ->
                player.sendMessage("An internal error has occurred, contact your administrator for support.")
        }
    }
}