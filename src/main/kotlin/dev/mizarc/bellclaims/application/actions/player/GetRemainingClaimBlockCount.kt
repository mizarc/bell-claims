package dev.mizarc.bellclaims.application.actions.player

import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PartitionRepository
import dev.mizarc.bellclaims.application.services.PlayerMetadataService
import java.util.UUID

class GetRemainingClaimBlockCount(private val claimRepository: ClaimRepository,
                                  private val partitionRepository: PartitionRepository,
                                  private val playerMetadataService: PlayerMetadataService) {
    fun execute(playerId: UUID): Int {
        val playerBlockLimit = playerMetadataService.getPlayerClaimBlockLimit(playerId)
        val playerBlockCount = claimRepository.getByPlayer(playerId).flatMap { playerClaim ->
            partitionRepository.getByClaim(playerClaim.id)
        }.sumOf { partition ->
            partition.getBlockCount()
        }
        return playerBlockLimit - playerBlockCount
    }
}