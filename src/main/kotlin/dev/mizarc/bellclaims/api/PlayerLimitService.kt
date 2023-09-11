package dev.mizarc.bellclaims.api

import org.bukkit.OfflinePlayer

interface PlayerLimitService {
    fun getTotalClaimCount(player: OfflinePlayer): Int
    fun getTotalClaimBlockCount(player: OfflinePlayer): Int
    fun getUsedClaimsCount(player: OfflinePlayer): Int
    fun getUsedClaimBlockCount(player: OfflinePlayer): Int
    fun getRemainingClaimCount(player: OfflinePlayer): Int
    fun getRemainingClaimBlockCount(player: OfflinePlayer): Int
}