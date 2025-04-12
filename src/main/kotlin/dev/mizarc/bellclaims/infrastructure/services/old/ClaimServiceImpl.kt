package dev.mizarc.bellclaims.infrastructure.services.old

import dev.mizarc.bellclaims.application.persistence.ClaimFlagRepository
import dev.mizarc.bellclaims.application.persistence.ClaimPermissionRepository
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PartitionRepository
import dev.mizarc.bellclaims.application.persistence.PlayerAccessRepository
import dev.mizarc.bellclaims.application.services.old.ClaimService
import dev.mizarc.bellclaims.domain.entities.Claim
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import java.util.UUID

class ClaimServiceImpl(private val claimRepo: ClaimRepository,
                       private val partitionRepo: PartitionRepository,
                       private val claimFlagRepo: ClaimFlagRepository,
                       private val claimPermissionRepo: ClaimPermissionRepository,
                       private val playerPermissionRepo: PlayerAccessRepository
): ClaimService {
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

    override fun transferClaim(claim: Claim, player: OfflinePlayer) {
        claim.owner = player
        claim.transferRequests = HashMap()

        claimRepo.update(claim)
        playerPermissionRepo.removeByPlayer(claim, player)
    }

    override fun addTransferRequest(claim: Claim, player: OfflinePlayer) {
        val currentTimestamp: Int = (System.currentTimeMillis() / 1000).toInt()
        val requestExpireTimestamp = currentTimestamp + (5 * 60)

        claim.transferRequests[player.uniqueId] = requestExpireTimestamp
    }

    override fun getTransferRequests(claim: Claim): java.util.HashMap<UUID, Int> {
        removeExpiredTransferRequests(claim)

        return claim.transferRequests
    }

    override fun playerHasTransferRequest(claim: Claim, player: OfflinePlayer): Boolean {
        removeExpiredTransferRequests(claim)

        return claim.transferRequests.containsKey(player.uniqueId);
    }

    private fun removeExpiredTransferRequests(claim: Claim) {
        val currentTimestamp: Int = (System.currentTimeMillis() / 1000).toInt()

        claim.transferRequests.forEach{ entry ->
            if (entry.value < currentTimestamp) {
                claim.transferRequests.remove(entry.key)
            }
        }
    }

    override fun deleteTransferRequest(claim: Claim, player: OfflinePlayer) {
        claim.transferRequests.remove(player.uniqueId)
    }

    override fun destroy(claim: Claim) {
        partitionRepo.removeByClaim(claim)
        claimFlagRepo.removeByClaim(claim)
        claimPermissionRepo.removeByClaim(claim)
        playerPermissionRepo.removeByClaim(claim)
        claimRepo.remove(claim)
    }
}