package dev.mizarc.bellclaims.application.enums

/**
 * Represents the result of changing a player's permissions for a claim.
 */
enum class PlayerPermissionChangeResult {
    CLAIM_DOES_NOT_EXIST,
    PLAYER_DOES_NOT_EXIST,
    SUCCESS,
    UNCHANGED
}