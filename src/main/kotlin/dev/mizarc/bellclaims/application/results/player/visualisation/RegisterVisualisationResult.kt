package dev.mizarc.bellclaims.application.results.player.visualisation

sealed class RegisterVisualisationResult {
    object Success: RegisterVisualisationResult()
    object ClaimNotFound: RegisterVisualisationResult()
}