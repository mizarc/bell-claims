package dev.mizarc.bellclaims.application.results.player.visualisation

sealed class UnregisterVisualisationResult {
    object Success: UnregisterVisualisationResult()
    object ClaimNotFound: UnregisterVisualisationResult()
}