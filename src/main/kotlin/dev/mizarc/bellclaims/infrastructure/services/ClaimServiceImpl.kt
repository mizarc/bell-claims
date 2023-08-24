package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.api.ClaimService
import dev.mizarc.bellclaims.api.PartitionService
import dev.mizarc.bellclaims.api.PlayerStateService
import dev.mizarc.bellclaims.api.enums.ClaimCreationResult
import dev.mizarc.bellclaims.api.enums.ClaimMoveResult
import dev.mizarc.bellclaims.domain.claims.*
import dev.mizarc.bellclaims.domain.partitions.*
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.Material
import java.util.UUID

class ClaimServiceImpl(private val claimRepo: ClaimRepository,
                       private val partitionRepo: PartitionRepository,
                       private val claimRuleRepo: ClaimRuleRepository,
                       private val claimPermissionRepo: ClaimPermissionRepository,
                       private val partitionService: PartitionService,
                       private val playerStateService: PlayerStateService): ClaimService {

    override fun getById(id: UUID): Claim? {
        return claimRepo.getById(id)
    }

    override fun getByPlayer(player: OfflinePlayer): Set<Claim> {
        return claimRepo.getByPlayer(player)
    }

    override fun getByLocation(location: Location): Claim? {
        return claimRepo.getByPosition(Position3D(location))
    }

    override fun getBlockCount(claim: Claim): Int {
        val claimPartitions = partitionRepo.getByClaim(claim)
        var count = 0
        for (partition in claimPartitions) {
            count += partition.area.getBlockCount()
        }
        return count
    }

    override fun getPartitionCount(claim: Claim): Int {
        return partitionRepo.getByClaim(claim).count()
    }

    override fun create(name: String, location: Location, player: OfflinePlayer): ClaimCreationResult {
        val claim = Claim(location.world.uid, player, Position3D(location), name)
        val area = Area(
            Position2D(claim.position.x - 5, claim.position.z - 5),
            Position2D(claim.position.x + 5, claim.position.z + 5))

        // Handle failure types
        if (location.block.type != Material.BELL) {
            return ClaimCreationResult.NOT_A_BELL
        }
        else if (partitionService.isAreaValid(area, claim)) {
            return ClaimCreationResult.TOO_CLOSE
        }
        else if (playerStateService.getRemainingClaimBlockCount(player) <= 0) {
            return ClaimCreationResult.OUT_OF_CLAIMS
        }

        // Store the claim and associated partition
        val partition = Partition(claim.id, area)
        claimRepo.add(claim)
        partitionRepo.add(partition)
        return ClaimCreationResult.SUCCESS
    }

    override fun changeName(claim: Claim, name: String) {
        claim.name = name
        claimRepo.update(claim)
    }

    override fun changeIcon(claim: Claim, material: Material) {
        claim.icon = material
        claimRepo.update(claim)
    }

    override fun changeLocation(claim: Claim, location: Location): ClaimMoveResult {
        // Handle failure types
        val partition = partitionService.getByLocation(location)
        
        if (partition == null || partition.claimId != claim.id) {
            return ClaimMoveResult.OUTSIDE_OF_AREA
        }

        claim.position = Position3D(location)
        claimRepo.update(claim)
        return ClaimMoveResult.SUCCESS
    }

    override fun destroy(claim: Claim) {
        partitionRepo.removeByClaim(claim)
        claimRuleRepo.removeByClaim(claim)
        claimPermissionRepo.removeByClaim(claim)
        claimRepo.remove(claim)
    }
}