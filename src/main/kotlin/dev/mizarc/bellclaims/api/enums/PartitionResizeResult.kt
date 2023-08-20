package dev.mizarc.bellclaims.api.enums

enum class PartitionResizeResult {
    SUCCESS,
    TOO_CLOSE,
    DISCONNECTED,
    OUT_OF_BLOCKS,
    OVERLAP
}