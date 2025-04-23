package dev.mizarc.bellclaims.application.results.player.visualisation

sealed class ScheduleClearVisualisationResult {
    object Success: ScheduleClearVisualisationResult()
    object PlayerNotVisualising: ScheduleClearVisualisationResult()
    object StorageError: ScheduleClearVisualisationResult()
}