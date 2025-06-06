package dev.mizarc.bellclaims.application.results.old

/**
 * Represents the result of adding a new partition to a claim.
 */
enum class PartitionCreationResult {
    INSUFFICIENT_BLOCKS,
    NOT_CONNECTED,
    OVERLAP,
    SUCCESS,
    TOO_CLOSE,
    TOO_SMALL
}