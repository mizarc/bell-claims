package dev.mizarc.bellclaims.application.results.claim

sealed class IsWorldActionAllowedResult {
    object Allowed: IsWorldActionAllowedResult()
    object Denied: IsWorldActionAllowedResult()
    object NoClaimFound: IsWorldActionAllowedResult()
    object NoAssociatedFlag: IsWorldActionAllowedResult()
    object StorageError: IsWorldActionAllowedResult()
}