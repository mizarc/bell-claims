package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import dev.mizarc.bellclaims.application.actions.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.UpdateClaimDescription
import dev.mizarc.bellclaims.application.enums.TextValidationErrorResult
import dev.mizarc.bellclaims.application.enums.UpdateClaimAttributeResult
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@CommandAlias("claim")
class DescriptionCommand : ClaimCommand(), KoinComponent {
    private val updateClaimDescription: UpdateClaimDescription by inject()
    private val getClaimDetails: GetClaimDetails by inject()

    @Subcommand("description")
    @CommandPermission("bellclaims.command.claim.description")
    fun onDescription(player: Player, description: String) {
        // Gets the partition at the player's current location
        val partition = getPartitionAtPlayer(player) ?: return
        if (!isPlayerHasClaimPermission(player, partition)) return

        // Update description and notify player of result
        val result = updateClaimDescription.execute(partition.claimId, description)
        when (result) {
            is UpdateClaimAttributeResult.Success -> {
                val claimName = getClaimDetails.execute(partition.claimId)?.name ?: "(Could not retrieve name)"
                player.sendMessage("§aNew description has been set for claim $claimName.")
            }
            is UpdateClaimAttributeResult.ClaimNotFound -> {
                player.sendMessage("Claim was not found.")
            }
            is UpdateClaimAttributeResult.InputTextInvalid -> {
                result.errors.forEach { error ->
                    when (error) {
                        is TextValidationErrorResult.ExceededCharacterLimit ->
                            player.sendMessage("Description of ${description.count()} characters exceeds character " +
                                    "limit of ${error.maxCharacters} characters.")
                        is TextValidationErrorResult.InvalidCharacters ->
                            player.sendMessage("Description contains invalid characters: ${error.invalidCharacters}")
                        is TextValidationErrorResult.ContainsBlacklistedWord ->
                            player.sendMessage("Description contains a blacklisted word: ${error.blacklistedWord}")
                        else ->
                            player.sendMessage("Description contains an unknown error.")
                    }
                }
            }
            is UpdateClaimAttributeResult.StorageError ->
                player.sendMessage("An internal error has occurred, contact your administrator for support.")
        }
    }
}