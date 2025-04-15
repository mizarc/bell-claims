package dev.mizarc.bellclaims.application.results.player

sealed class RegisterVisualisationResult {
    object Success: RegisterVisualisationResult()
    object ClaimNotFound: RegisterVisualisationResult()
}