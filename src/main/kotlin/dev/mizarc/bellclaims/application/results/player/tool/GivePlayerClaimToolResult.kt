package dev.mizarc.bellclaims.application.results.player.tool

import dev.mizarc.bellclaims.application.results.player.DoesPlayerHaveClaimOverrideResult

sealed class GivePlayerClaimToolResult {
    object Success: GivePlayerClaimToolResult()
    object PlayerAlreadyHasTool: GivePlayerClaimToolResult()
    object PlayerNotFound: GivePlayerClaimToolResult()
}