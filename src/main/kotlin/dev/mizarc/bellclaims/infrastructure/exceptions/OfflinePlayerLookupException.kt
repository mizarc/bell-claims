package dev.mizarc.bellclaims.infrastructure.exceptions

/**
 * Thrown when a synchronous metadata lookup for an offline player is attempted.
 * This signals that the caller should perform the lookup asynchronously instead.
 */
class OfflinePlayerLookupException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)

