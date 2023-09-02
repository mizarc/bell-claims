package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.api.ClaimService
import dev.mizarc.bellclaims.domain.claims.*
import dev.mizarc.bellclaims.domain.flags.ClaimFlagRepository
import dev.mizarc.bellclaims.domain.partitions.*
import dev.mizarc.bellclaims.domain.permissions.ClaimPermissionRepository
import dev.mizarc.bellclaims.domain.permissions.PlayerAccessRepository
import org.bukkit.OfflinePlayer
import org.bukkit.Material
import java.util.UUID

class ClaimServiceImpl(private val claimRepo: ClaimRepository,
                       private val partitionRepo: PartitionRepository,
                       private val claimFlagRepo: ClaimFlagRepository,
                       private val claimPermissionRepo: ClaimPermissionRepository,
                       private val playerPermissionRepo: PlayerAccessRepository): ClaimService {
    override fun getById(id: UUID): Claim? {
        return claimRepo.getById(id)
    }

    override fun getByPlayer(player: OfflinePlayer): Set<Claim> {
        return claimRepo.getByPlayer(player)
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

    override fun changeName(claim: Claim, name: String) {
        claim.name = name
        claimRepo.update(claim)
    }

    override fun changeDescription(claim: Claim, description: String) {
        claim.description = description
        claimRepo.update(claim)
    }

    override fun changeIcon(claim: Claim, material: Material) {
        claim.icon = material
        claimRepo.update(claim)
    }

    override fun destroy(claim: Claim) {
        partitionRepo.removeByClaim(claim)
        claimFlagRepo.removeByClaim(claim)
        claimPermissionRepo.removeByClaim(claim)
        playerPermissionRepo.removeByClaim(claim)
        claimRepo.remove(claim)
    }
}