package dev.mizarc.bellclaims.application.services

import java.util.UUID

interface PlayerMetadataService {
    fun getPlayerClaimLimit(playerId: UUID): Int
    fun getPlayerClaimBlockLimit(playerId: UUID): Int
    suspend fun getPlayerClaimLimitAsync(playerId: UUID): Int
    suspend fun getPlayerClaimBlockLimitAsync(playerId: UUID): Int
}