package dev.mizarc.bellclaims.application.actions

import dev.mizarc.bellclaims.application.enums.ToggleClaimOverrideResult
import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.PlayerStateRepository
import java.util.UUID

class ToggleClaimOverride(private val playerStateRepository: PlayerStateRepository) {
    fun execute(playerId: UUID): ToggleClaimOverrideResult {
        val playerState = playerStateRepository.get(playerId) ?: return ToggleClaimOverrideResult.PlayerNotFound
        val newOverrideValue = !playerState.claimOverride
        playerState.claimOverride = newOverrideValue

        return try {
            playerStateRepository.update(playerState)
            ToggleClaimOverrideResult.Success(newOverrideValue)
        } catch (error: DatabaseOperationException) {
            println("Error has occurred trying to save to the database")
            ToggleClaimOverrideResult.StorageError
        }
    }
}