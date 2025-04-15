package dev.mizarc.bellclaims.application.results.player.visualisation

sealed class GetVisualiserModeResult {
    data class Success(val visualiserMode: Int): GetVisualiserModeResult()
    object StorageError: GetVisualiserModeResult()
}