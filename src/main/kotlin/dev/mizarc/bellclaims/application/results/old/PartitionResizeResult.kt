package dev.mizarc.bellclaims.application.results.old

/**
 * Represents the result of resizing an existing partition.
 */
enum class PartitionResizeResult {
    EXPOSED_CLAIM_HUB,
    DISCONNECTED,
    INSUFFICIENT_BLOCKS,
    OVERLAP,
    SUCCESS,
    TOO_SMALL,
    TOO_CLOSE
}