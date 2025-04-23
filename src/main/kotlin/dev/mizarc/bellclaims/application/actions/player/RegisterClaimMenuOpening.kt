package dev.mizarc.bellclaims.application.actions.player

import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PlayerStateRepository
import dev.mizarc.bellclaims.application.results.player.RegisterClaimMenuOpeningResult
import dev.mizarc.bellclaims.domain.entities.PlayerState
import java.util.UUID

class RegisterClaimMenuOpening(private val playerStateRepository: PlayerStateRepository,
                               private val claimRepository: ClaimRepository) {
    fun execute(playerId: UUID, claimId: UUID): RegisterClaimMenuOpeningResult {
        // Check if claim exists
        claimRepository.getById(claimId) ?: return RegisterClaimMenuOpeningResult.ClaimNotFound

        // Get or create player state if it doesn't exist
        var playerState = playerStateRepository.get(playerId)
        if (playerState == null) {
            playerState = PlayerState(playerId)
            playerStateRepository.add(playerState)
        }

        // Set currently open claim menu
        playerState.isInClaimMenu = claimId
        playerStateRepository.update(playerState)
        return RegisterClaimMenuOpeningResult.Success
    }
}