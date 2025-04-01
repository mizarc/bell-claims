package dev.mizarc.bellclaims.application.enums

/**
 * Represents the result of creating a claim.
 */
enum class ClaimCreationResult {
    SUCCESS,
    TOO_CLOSE,
    OUT_OF_CLAIMS,
    OUT_OF_CLAIM_BLOCKS,
    NOT_A_BELL
}