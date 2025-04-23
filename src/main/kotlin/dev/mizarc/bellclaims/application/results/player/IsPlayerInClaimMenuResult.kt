package dev.mizarc.bellclaims.application.results.player

sealed class IsPlayerInClaimMenuResult {
    data class Success(val isInClaimMenu: Boolean): IsPlayerInClaimMenuResult()
    object StorageError: IsPlayerInClaimMenuResult()
}