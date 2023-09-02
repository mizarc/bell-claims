package dev.mizarc.bellclaims.api.enums

enum class PartitionCreationResult {
    INSUFFICIENT_BLOCKS,
    NOT_CONNECTED,
    OVERLAP,
    SUCCESS,
    TOO_CLOSE,
    TOO_SMALL
}