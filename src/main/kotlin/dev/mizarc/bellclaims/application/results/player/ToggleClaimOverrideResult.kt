package dev.mizarc.bellclaims.application.results.player

sealed class ToggleClaimOverrideResult {
    data class Success(val isOverrideEnabled: Boolean) : ToggleClaimOverrideResult()
    object PlayerNotFound: ToggleClaimOverrideResult()
    object StorageError: ToggleClaimOverrideResult()
}