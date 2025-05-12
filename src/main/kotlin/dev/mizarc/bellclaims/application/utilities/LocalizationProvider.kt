package dev.mizarc.bellclaims.application.utilities

import java.util.Locale

interface LocalizationProvider {
    /**
     * Gets a localized message for the config defined locale.
     *
     * @param key The message key.
     * @param args Optional arguments for formatting the message.
     * @return The localized and formatted message. Handles missing keys internally.
     */
    fun get(key: String, vararg args: Any?): String

    /**
     * Gets a localized message for a specified locale.
     *
     * @param locale The specific locale to use.
     * @param key The message key.
     * @param args Optional arguments for formatting the message.
     * @return The localized and formatted message. Handles missing keys internally.
     */
    fun get(locale: Locale, key: String, vararg args: Any?): String
}