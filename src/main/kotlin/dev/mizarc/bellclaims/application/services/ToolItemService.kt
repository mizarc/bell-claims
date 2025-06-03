package dev.mizarc.bellclaims.application.services

import dev.mizarc.bellclaims.domain.entities.Claim
import java.util.UUID

interface ToolItemService {
    fun giveClaimTool(playerId: UUID): Boolean
    fun giveClaimMoveTool(playerId: UUID, claim: Claim): Boolean
    fun isPlayerHoldingClaimTool(playerId: UUID): Boolean
    fun isPlayerHoldingClaimMoveTool(playerId: UUID): Boolean
    fun getClaimIdFromPlayerMoveTool(playerId: UUID): String?
    fun removeClaimToolCommand(playerId: UUID): Boolean
    fun removeClaimMoveToolCommand(playerId: UUID, claim: Claim): Boolean
}