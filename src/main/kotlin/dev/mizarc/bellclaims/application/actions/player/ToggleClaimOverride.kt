package dev.mizarc.bellclaims.application.actions.player

import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.PlayerStateRepository
import dev.mizarc.bellclaims.application.results.player.ToggleClaimOverrideResult
import java.util.UUID

class ToggleClaimOverride(private val playerStateRepository: PlayerStateRepository) {
    fun execute(playerId: UUID): ToggleClaimOverrideResult {
        // Check if player state exists
        val playerState = playerStateRepository.get(playerId) ?: return ToggleClaimOverrideResult.PlayerNotFound

        // Toggle override and persist to storage
        playerState.claimOverride = !playerState.claimOverride
        try {
            playerStateRepository.update(playerState)
            return ToggleClaimOverrideResult.Success(playerState.claimOverride)
        } catch (error: DatabaseOperationException) {
            println("Error has occurred trying to save to the database")
            return ToggleClaimOverrideResult.StorageError
        }
    }
}