package dev.mizarc.bellclaims.application.results

sealed class DoesPlayerHaveClaimOverrideResult {
    data class Success(val hasOverride: Boolean): DoesPlayerHaveClaimOverrideResult()
    object StorageError: DoesPlayerHaveClaimOverrideResult()
}