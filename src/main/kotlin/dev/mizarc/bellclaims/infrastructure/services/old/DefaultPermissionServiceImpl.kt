package dev.mizarc.bellclaims.infrastructure.services.old

import dev.mizarc.bellclaims.application.services.old.DefaultPermissionService
import dev.mizarc.bellclaims.application.results.DefaultPermissionChangeResult
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.application.persistence.ClaimPermissionRepository
import dev.mizarc.bellclaims.domain.values.ClaimPermission

class DefaultPermissionServiceImpl(private val permissionRepo: ClaimPermissionRepository): DefaultPermissionService {

    override fun getByClaim(claim: Claim): Set<ClaimPermission> {
        return permissionRepo.getByClaim(claim)
    }

    override fun add(claim: Claim, permission: ClaimPermission): DefaultPermissionChangeResult {
        if (permission in permissionRepo.getByClaim(claim))
            return DefaultPermissionChangeResult.UNCHANGED

        permissionRepo.add(claim, permission)
        return DefaultPermissionChangeResult.SUCCESS
    }

    override fun addAll(claim: Claim): DefaultPermissionChangeResult {
        val permissionsToAdd = ClaimPermission.entries.toMutableList() - getByClaim(claim)
        if (permissionsToAdd.isEmpty()) return DefaultPermissionChangeResult.UNCHANGED

        for (permission in permissionsToAdd) {
            permissionRepo.add(claim, permission)
        }
        return DefaultPermissionChangeResult.SUCCESS
    }

    override fun remove(claim: Claim, permission: ClaimPermission): DefaultPermissionChangeResult {
        if (permission !in permissionRepo.getByClaim(claim))
            return DefaultPermissionChangeResult.UNCHANGED

        permissionRepo.remove(claim, permission)
        return DefaultPermissionChangeResult.SUCCESS
    }

    override fun removeAll(claim: Claim): DefaultPermissionChangeResult {
        val permissionsToRemove = getByClaim(claim)
        if (permissionsToRemove.isEmpty()) return DefaultPermissionChangeResult.UNCHANGED

        for (permission in permissionsToRemove) {
            permissionRepo.remove(claim, permission)
        }
        return DefaultPermissionChangeResult.SUCCESS
    }
}