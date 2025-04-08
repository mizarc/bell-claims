package dev.mizarc.bellclaims.application.results

/**
 * Represents the result of changing the default permissions of a claim.
 */
enum class DefaultPermissionChangeResult {
    SUCCESS,
    CLAIM_DOES_NOT_EXIST,
    UNCHANGED
}