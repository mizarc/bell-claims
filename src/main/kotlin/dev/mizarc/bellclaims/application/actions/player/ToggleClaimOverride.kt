package dev.mizarc.bellclaims.application.actions.player

import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.PlayerStateRepository
import dev.mizarc.bellclaims.application.results.player.ToggleClaimOverrideResult
import dev.mizarc.bellclaims.domain.entities.PlayerState
import java.util.UUID

class ToggleClaimOverride(private val playerStateRepository: PlayerStateRepository) {
    fun execute(playerId: UUID): ToggleClaimOverrideResult {
        // Get or create player state if it doesn't exist
        var playerState = playerStateRepository.get(playerId)
        if (playerState == null) {
            playerState = PlayerState(playerId)
            playerStateRepository.add(playerState)
        }

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