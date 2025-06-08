package dev.mizarc.bellclaims.application.services

import dev.mizarc.bellclaims.domain.entities.Claim
import java.util.UUID

interface ToolItemService {
    fun giveClaimTool(playerId: UUID): Boolean
    fun giveMoveTool(playerId: UUID, claim: Claim): Boolean
    fun doesPlayerHaveClaimTool(playerId: UUID): Boolean
    fun doesPlayerHaveMoveTool(playerId: UUID, claim: Claim): Boolean
    fun isClaimTool(itemData: Map<String, String>?): Boolean
    fun isMoveTool(itemData: Map<String, String>?): Boolean
    fun getClaimIdFromPlayerMoveTool(itemData: Map<String, String>): String?
}