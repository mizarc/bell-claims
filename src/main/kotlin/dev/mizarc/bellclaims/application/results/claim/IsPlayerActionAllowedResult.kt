package dev.mizarc.bellclaims.application.results.claim

import dev.mizarc.bellclaims.domain.entities.Claim

sealed class IsPlayerActionAllowedResult {
    data class Allowed(val claim: Claim): IsPlayerActionAllowedResult()
    data class Denied(val claim: Claim): IsPlayerActionAllowedResult()
    object NoClaimFound: IsPlayerActionAllowedResult()
    object NoAssociatedPermission: IsPlayerActionAllowedResult()
    object StorageError: IsPlayerActionAllowedResult()
}