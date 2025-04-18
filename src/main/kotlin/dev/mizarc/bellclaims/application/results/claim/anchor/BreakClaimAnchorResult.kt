package dev.mizarc.bellclaims.application.results.claim.anchor

sealed class BreakClaimAnchorResult {
    object Success: BreakClaimAnchorResult()
    data class ClaimBreaking(val remainingBreaks: Int): BreakClaimAnchorResult()
    object ClaimNotFound: BreakClaimAnchorResult()
    object StorageError: BreakClaimAnchorResult()
}