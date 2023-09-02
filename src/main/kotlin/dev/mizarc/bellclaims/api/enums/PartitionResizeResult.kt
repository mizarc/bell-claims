package dev.mizarc.bellclaims.api.enums

/**
 * An enum representing the result of a partition resizing operation.
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