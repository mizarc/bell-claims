package dev.mizarc.bellclaims.application.results.claim

import dev.mizarc.bellclaims.domain.entities.Claim

sealed class BreakClaimAnchorResult {
    object Success: BreakClaimAnchorResult()
    data class ClaimBreaking(val remainingBreaks: Int): BreakClaimAnchorResult()
    object ClaimNotFound: BreakClaimAnchorResult()
    object StorageError: BreakClaimAnchorResult()
}