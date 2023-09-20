package dev.mizarc.bellclaims.api.enums

/**
 * Represents the result of changing the default permissions of a claim.
 */
enum class DefaultPermissionChangeResult {
    SUCCESS,
    CLAIM_DOES_NOT_EXIST,
    UNCHANGED
}