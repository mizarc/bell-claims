package dev.mizarc.bellclaims.application.results.player

sealed class UnregisterVisualisationResult {
    object Success: UnregisterVisualisationResult()
    object ClaimNotFound: UnregisterVisualisationResult()
}