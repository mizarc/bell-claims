package dev.mizarc.bellclaims.application.results.player

class IsPlayerVisualisingResult {
    data class Success(val isVisualising: Boolean): IsPlayerVisualisingResult()
    object StorageError: IsPlayerVisualisingResult()
}