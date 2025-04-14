package dev.mizarc.bellclaims.application.actions.player

import dev.mizarc.bellclaims.application.persistence.PlayerStateRepository
import dev.mizarc.bellclaims.application.results.player.IsPlayerInClaimMenuResult
import dev.mizarc.bellclaims.domain.entities.PlayerState
import java.util.UUID

class IsPlayerInClaimMenu(private val playerStateRepository: PlayerStateRepository) {
    fun execute(playerId: UUID, claimId: UUID): IsPlayerInClaimMenuResult {
        // Get or create player state if it doesn't exist
        var playerState = playerStateRepository.get(playerId)
        if (playerState == null) {
            playerState = PlayerState(playerId)
            playerStateRepository.add(playerState)
        }

        if (claimId == playerState.isInClaimMenu) {
            return IsPlayerInClaimMenuResult.Success(true)
        }
        return IsPlayerInClaimMenuResult.Success(false)
    }
}