package dev.mizarc.bellclaims.application.actions

import dev.mizarc.bellclaims.application.persistence.PlayerStateRepository
import dev.mizarc.bellclaims.application.results.DoesPlayerHaveClaimOverrideResult
import dev.mizarc.bellclaims.domain.entities.PlayerState
import java.util.UUID

class DoesPlayerHaveClaimOverride(private val playerStateRepository: PlayerStateRepository) {
    fun execute(playerId: UUID): DoesPlayerHaveClaimOverrideResult {
        // Get or create player state if it doesn't exist
        var playerState = playerStateRepository.get(playerId)
        if (playerState == null) {
            playerState = PlayerState(playerId)
            playerStateRepository.add(playerState)
        }

        return DoesPlayerHaveClaimOverrideResult.Success(playerState.claimOverride)
    }
}