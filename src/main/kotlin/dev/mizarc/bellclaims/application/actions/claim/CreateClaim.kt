package dev.mizarc.bellclaims.application.actions.claim

import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PartitionRepository
import dev.mizarc.bellclaims.application.results.claim.CreateClaimResult
import dev.mizarc.bellclaims.application.services.PlayerMetadataService
import dev.mizarc.bellclaims.application.services.WorldManipulationService
import dev.mizarc.bellclaims.config.MainConfig
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.entities.Partition
import dev.mizarc.bellclaims.domain.values.Area
import dev.mizarc.bellclaims.domain.values.Position2D
import dev.mizarc.bellclaims.domain.values.Position3D
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.floor

class CreateClaim(private val claimRepository: ClaimRepository, private val partitionRepository: PartitionRepository,
                  private val playerMetadataService: PlayerMetadataService,
                  private val worldManipulationService: WorldManipulationService,
                  private val config: MainConfig
) {

    fun execute(playerId: UUID, name: String, position3D: Position3D, worldId: UUID): CreateClaimResult {
        val claims = claimRepository.getByPlayer(playerId)
        if (claims.count() >= playerMetadataService.getPlayerClaimLimit(playerId)) {
            return CreateClaimResult.LimitExceeded
        }

        // Check if input name is blank
        if (name.isBlank()) {
            return CreateClaimResult.NameCannotBeBlank
        }

        // Check if name already exists
        val existingClaim = claimRepository.getByName(playerId, name)
        if (existingClaim != null) {
            return CreateClaimResult.NameAlreadyExists
        }

        // Generates the partition area based on config
        val areaSize = config.initialClaimSize
        val offsetMin = (areaSize - 1) / 2
        val offsetMax = areaSize / 2
        val area = Area(
            Position2D(position3D.x - offsetMin, position3D.z - offsetMin),
            Position2D(position3D.x + offsetMax, position3D.z + offsetMax)
        )

        // Validate area is within world border
        if (!worldManipulationService.isInsideWorldBorder(worldId, area)) {
            return CreateClaimResult.TooCloseToWorldBorder
        }

        // Creates new claim and partition
        val newClaim = Claim(worldId, playerId, position3D, name)
        val partition = Partition(newClaim.id, area)
        claimRepository.add(newClaim)
        partitionRepository.add(partition)
        return CreateClaimResult.Success(newClaim)
    }
}