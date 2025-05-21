package dev.mizarc.bellclaims.application.utilities

import java.util.UUID

interface LocalizationProvider {
    /**
     * Gets a localized message for a specified locale.
     *
     * @param playerId The player id to use to fetch the player's locale.
     * @param key The message key.
     * @param args Optional arguments for formatting the message.
     * @return The localized and formatted message.
     */
    fun get(playerId: UUID, key: String, vararg args: Any?): String

    /**
     * Gets a localized message using the server locale, used for console logs.
     *
     * @param key The message key.
     * @param args Optional arguments for formatting the message.
     * @return The localized and formatted message.
     */
    fun getConsole(key: String, vararg args: Any?): String
}