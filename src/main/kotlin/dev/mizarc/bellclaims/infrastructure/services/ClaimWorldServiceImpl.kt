package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.api.ClaimWorldService
import dev.mizarc.bellclaims.api.PartitionService
import dev.mizarc.bellclaims.api.PlayerLimitService
import dev.mizarc.bellclaims.api.PlayerStateService
import dev.mizarc.bellclaims.api.enums.ClaimCreationResult
import dev.mizarc.bellclaims.api.enums.ClaimMoveResult
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.claims.ClaimRepository
import dev.mizarc.bellclaims.domain.partitions.Area
import dev.mizarc.bellclaims.domain.partitions.Position2D
import dev.mizarc.bellclaims.domain.partitions.Position3D
import dev.mizarc.bellclaims.infrastructure.persistence.Config
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import kotlin.math.ceil
import kotlin.math.floor

class ClaimWorldServiceImpl(private val claimRepo: ClaimRepository,
                            private val partitionService: PartitionService,
                            private val playerLimitService: PlayerLimitService,
                            private val config: Config): ClaimWorldService {
    override fun isNewLocationValid(location: Location): Boolean {
        val area = Area(
            Position2D(location.blockX - 5, location.blockZ - 5),
            Position2D(location.blockX + 5, location.blockZ + 5))
        return partitionService.isAreaValid(area, location.world)
    }

    override fun isMoveLocationValid(claim: Claim, location: Location): Boolean {
        val partition = partitionService.getByLocation(location) ?: return false
        return partition.claimId == claim.id
    }

    override fun getByLocation(location: Location): Claim? {
        return claimRepo.getByPosition(Position3D(location), location.world.uid)
    }

    override fun create(name: String, location: Location, player: OfflinePlayer): ClaimCreationResult {
        val halfSize = (config.initialClaimSize.toFloat() - 1) / 2
        val area = Area(
            Position2D((location.blockX - floor(halfSize)).toInt(), (location.blockZ - floor(halfSize)).toInt()),
            Position2D((location.blockX + ceil(halfSize)).toInt(),(location.blockZ + ceil(halfSize)).toInt()))

        // Handle failure types
        if (location.block.type != Material.BELL) return ClaimCreationResult.NOT_A_BELL
        else if (!partitionService.isAreaValid(area, location.world)) return ClaimCreationResult.TOO_CLOSE
        else if (playerLimitService.getRemainingClaimCount(player) < 1) return ClaimCreationResult.OUT_OF_CLAIMS
        else if (playerLimitService.getRemainingClaimBlockCount(player) < area.getBlockCount())
            return ClaimCreationResult.OUT_OF_CLAIM_BLOCKS

        // Store the claim and associated partition
        val claim = Claim(location.world.uid, player, Position3D(location), name)
        claimRepo.add(claim)
        partitionService.append(area, claim)
        return ClaimCreationResult.SUCCESS
    }

    override fun move(claim: Claim, location: Location): ClaimMoveResult {
        if (!isMoveLocationValid(claim, location)) return ClaimMoveResult.OUTSIDE_OF_AREA
        claim.position = Position3D(location)
        claimRepo.update(claim)
        return ClaimMoveResult.SUCCESS
    }
}