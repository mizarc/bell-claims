package dev.mizarc.bellclaims.infrastructure.utilities

import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import dev.mizarc.bellclaims.config.MainConfig
import java.text.MessageFormat
import java.util.*

class LocalizationProviderResourceBundle(private val config: MainConfig): LocalizationProvider {
    override fun get(key: String, vararg args: Any?): String {
        val locale = try {
            Locale.forLanguageTag(config.pluginLanguage.replace('_', '-'))

        } catch (error: NullPointerException) {
            Locale.getDefault()
        }
        return get(locale, key, *args)
    }

    override fun get(locale: Locale, key: String, vararg args: Any?): String {
        // ResourceBundle.getBundle handles the standard Java locale fallback chain
        // (e.g., fr_CA -> fr -> default locale -> root bundle) automatically.
        val bundle: ResourceBundle
        try {
            bundle = ResourceBundle.getBundle("lang", locale,
                ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_PROPERTIES))
        } catch (e: MissingResourceException) {
            return key
        }

        // Get the raw message pattern string from the found bundle(s).
        // Return the key itself if a result cannot be found.
        val pattern: String
        try {
            pattern = bundle.getString(key)
        } catch (e: MissingResourceException) {
            return key
        }

        // Format the message if arguments are provided. Return the pattern
        // without the arguments filled in if provided arguments are invalid.
        return try {
            if (args.isNotEmpty()) {
                MessageFormat.format(pattern, *args)
            } else {
                pattern
            }
        } catch (e: IllegalArgumentException) {
            pattern
        }
    }
}