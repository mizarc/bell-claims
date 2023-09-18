package dev.mizarc.bellclaims.api

import org.bukkit.OfflinePlayer

/**
 * A service that allows you to find player claim limits.
 */
interface PlayerLimitService {
    /**
     * Gets the total number of claims that a player can own.
     *
     * @param player The player to query.
     * @return The number of claims this player can have in total.
     */
    fun getTotalClaimCount(player: OfflinePlayer): Int

    /**
     * Gets the total number of claims blocks the player's claims can occupy.
     *
     * @param player The player to query.
     * @return The number of claim blocks this player can have in total.
     */
    fun getTotalClaimBlockCount(player: OfflinePlayer): Int

    /**
     * Gets the number of claims the player owns.
     *
     * @param player The player to query.
     * @return The number of claims the player currently owns.
     */
    fun getUsedClaimsCount(player: OfflinePlayer): Int

    /**
     * Gets the number of claim blocks the player's claims are using.
     *
     * @param player The player to query.
     * @return The number of claims blocks the player currently uses.
     */
    fun getUsedClaimBlockCount(player: OfflinePlayer): Int

    /**
     * Gets the remaining number of claims the player can create.
     *
     * @param player The player to query.
     * @return The remaining number of claims the player can create.
     */
    fun getRemainingClaimCount(player: OfflinePlayer): Int

    /**
     * Gets the remaining number of claims blocks the player can use for their claims.
     *
     * @param player The player to query.
     * @return The remaining number of claims blocks the player can use.
     */
    fun getRemainingClaimBlockCount(player: OfflinePlayer): Int
}