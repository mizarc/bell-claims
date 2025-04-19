package dev.mizarc.bellclaims.application.results.claim

sealed class IsPlayerActionAllowedResult {
    object Allowed: IsPlayerActionAllowedResult()
    object Denied: IsPlayerActionAllowedResult()
    object NoClaimFound: IsPlayerActionAllowedResult()
    object NoAssociatedPermission: IsPlayerActionAllowedResult()
    object StorageError: IsPlayerActionAllowedResult()
}