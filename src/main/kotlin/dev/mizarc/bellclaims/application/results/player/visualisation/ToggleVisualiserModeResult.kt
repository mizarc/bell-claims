package dev.mizarc.bellclaims.application.results.player.visualisation

sealed class ToggleVisualiserModeResult {
    data class Success(val visualiserMode: Int) : ToggleVisualiserModeResult()
    data class OnCooldown(val cooldownTime: Int): ToggleVisualiserModeResult()
}