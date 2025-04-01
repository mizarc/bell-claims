package dev.mizarc.bellclaims.application.enums

/**
 * Represents the result of changing a claim's flags.
 */
enum class FlagChangeResult {
    CLAIM_DOES_NOT_EXIST,
    SUCCESS,
    UNCHANGED
}