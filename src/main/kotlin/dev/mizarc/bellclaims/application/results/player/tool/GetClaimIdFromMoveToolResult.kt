package dev.mizarc.bellclaims.application.results.player.tool

import java.util.UUID

sealed class GetClaimIdFromMoveToolResult {
    data class Success(val claimId: UUID): GetClaimIdFromMoveToolResult()
    object NotMoveTool: GetClaimIdFromMoveToolResult()
}