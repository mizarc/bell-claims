package dev.mizarc.bellclaims.application.actions

import dev.mizarc.bellclaims.application.enums.ToggleClaimOverrideResult
import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.PlayerStateRepository
import java.util.UUID

class ToggleClaimOverride(private val playerStateRepository: PlayerStateRepository) {
    fun execute(playerId: UUID): ToggleClaimOverrideResult {
        // Check if player state exists
        val playerState = playerStateRepository.get(playerId) ?: return ToggleClaimOverrideResult.PlayerNotFound

        // Toggle override and persist to storage
        val newOverrideValue = !playerState.claimOverride
        playerState.claimOverride = newOverrideValue
        try {
            playerStateRepository.update(playerState)
            return ToggleClaimOverrideResult.Success(newOverrideValue)
        } catch (error: DatabaseOperationException) {
            println("Error has occurred trying to save to the database")
            return ToggleClaimOverrideResult.StorageError
        }
    }
}