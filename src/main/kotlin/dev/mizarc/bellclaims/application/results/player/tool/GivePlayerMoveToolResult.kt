package dev.mizarc.bellclaims.application.results.player.tool

sealed class GivePlayerMoveToolResult {
    object Success: GivePlayerMoveToolResult()
    object PlayerAlreadyHasTool: GivePlayerMoveToolResult()
    object ClaimNotFound: GivePlayerMoveToolResult()
    object PlayerNotFound: GivePlayerMoveToolResult()
}