package dev.mizarc.bellclaims.application.results.claim.flags

import dev.mizarc.bellclaims.application.enums.DisableClaimFlagResult

sealed class DoesClaimHaveFlagResult {
    data class Success(val hasFlag: Boolean) : DoesClaimHaveFlagResult()
    object ClaimNotFound : DoesClaimHaveFlagResult()
    object StorageError: DoesClaimHaveFlagResult()
}