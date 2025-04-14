package dev.mizarc.bellclaims.application.results.player

sealed class UnregisterClaimMenuOpeningResult {
    object Success: UnregisterClaimMenuOpeningResult()
    object NotRegistered: UnregisterClaimMenuOpeningResult()
    object ClaimNotFound: UnregisterClaimMenuOpeningResult()
}