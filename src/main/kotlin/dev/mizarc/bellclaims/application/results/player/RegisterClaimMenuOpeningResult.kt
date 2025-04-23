package dev.mizarc.bellclaims.application.results.player

sealed class RegisterClaimMenuOpeningResult {
    object Success: RegisterClaimMenuOpeningResult()
    object ClaimNotFound: RegisterClaimMenuOpeningResult()
}