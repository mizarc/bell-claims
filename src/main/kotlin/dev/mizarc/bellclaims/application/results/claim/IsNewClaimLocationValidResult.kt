package dev.mizarc.bellclaims.application.results.claim

sealed class IsNewClaimLocationValidResult {
    object Valid: IsNewClaimLocationValidResult()
    object Overlap: IsNewClaimLocationValidResult()
    object TooClose: IsNewClaimLocationValidResult()
    object OutsideWorldBorder: IsNewClaimLocationValidResult()
    object StorageError: IsNewClaimLocationValidResult()
}