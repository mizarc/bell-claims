package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.api.DefaultPermissionService
import dev.mizarc.bellclaims.api.enums.DefaultPermissionChangeResult
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.claims.ClaimPermissionRepository
import dev.mizarc.bellclaims.interaction.listeners.ClaimPermission

class DefaultPermissionServiceImpl(private val permissionRepo: ClaimPermissionRepository): DefaultPermissionService {

    override fun getByClaim(claim: Claim): Set<ClaimPermission> {
        return permissionRepo.getByClaim(claim)
    }

    override fun add(claim: Claim, permission: ClaimPermission): DefaultPermissionChangeResult {
        if (permission in permissionRepo.getByClaim(claim))
            return DefaultPermissionChangeResult.PERMISSION_STATE_UNCHANGED

        permissionRepo.add(claim, permission)
        return DefaultPermissionChangeResult.SUCCESS
    }

    override fun addAll(claim: Claim): DefaultPermissionChangeResult {
        val permissionsToAdd = ClaimPermission.values().toMutableList() - getByClaim(claim)
        if (permissionsToAdd.isEmpty()) DefaultPermissionChangeResult.PERMISSION_STATE_UNCHANGED

        for (permission in permissionsToAdd) {
            permissionRepo.add(claim, permission)
        }
        return DefaultPermissionChangeResult.SUCCESS
    }

    override fun remove(claim: Claim, permission: ClaimPermission): DefaultPermissionChangeResult {
        if (permission !in permissionRepo.getByClaim(claim))
            return DefaultPermissionChangeResult.PERMISSION_STATE_UNCHANGED

        permissionRepo.remove(claim, permission)
        return DefaultPermissionChangeResult.SUCCESS
    }

    override fun removeAll(claim: Claim): DefaultPermissionChangeResult {
        val permissionsToRemove = getByClaim(claim)
        if (permissionsToRemove.isEmpty()) DefaultPermissionChangeResult.PERMISSION_STATE_UNCHANGED

        for (permission in permissionsToRemove) {
            permissionRepo.remove(claim, permission)
        }
        return DefaultPermissionChangeResult.SUCCESS
    }
}