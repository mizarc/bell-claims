package dev.mizarc.bellclaims.application.actions

import dev.mizarc.bellclaims.application.persistence.ClaimPermissionRepository
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import java.util.*

class GetClaimPermissions(private val permissionRepository: ClaimPermissionRepository) {
    fun execute(claimId: UUID): List<ClaimPermission> {
        return permissionRepository.getByClaim(claimId).toList()
    }
}