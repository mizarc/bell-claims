package dev.mizarc.bellclaims.application.utilities

interface LocalizationProvider {
    /**
     * Gets a localized message for the config defined locale.
     *
     * @param key The message key.
     * @param args Optional arguments for formatting the message.
     * @return The localized and formatted message. Handles missing keys internally.
     */
    fun get(key: String, vararg args: Any?): String
}