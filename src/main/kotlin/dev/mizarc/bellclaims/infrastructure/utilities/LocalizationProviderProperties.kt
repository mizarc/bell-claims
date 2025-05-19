package dev.mizarc.bellclaims.infrastructure.utilities

import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import dev.mizarc.bellclaims.config.MainConfig
import java.io.File
import java.text.MessageFormat
import java.util.*

class LocalizationProviderProperties(private val config: MainConfig,
                                     private val dataFolder: File): LocalizationProvider {
    private val languages: MutableMap<String, Properties> = mutableMapOf()
    private val baseDefaultLanguageCode = "en"

    init {
        loadLayeredProperties()
    }

    override fun get(locale: String, key: String, vararg args: Any?): String {
        // Step 1: Try to get the bundle for the exact requested language code
        var properties = languages[locale]

        // Step 2: If the exact language code bundle is not found, try the base language
        if (properties == null) {
            // Derive the base language code (e.g., "en" from "en_UK").
            val requestedLocale = Locale.forLanguageTag(locale.replace('_', '-'))
            val baseLanguage = requestedLocale.language

            // If the derived base language code is different from the requested code and is not empty,
            // try to get the bundle for this base language.
            if (baseLanguage != locale && baseLanguage.isNotEmpty()) {
                properties = languages[baseLanguage]
            }
        }

        // Step 3: If still not found, fallback to the server's configured default language.
        if (properties == null) {
            properties = languages[config.pluginLanguage]
        }

        // Step 4: If still not found, fallback to the hardcoded base default language ("en").
        if (properties == null) {
            properties = languages[baseDefaultLanguageCode]
        }

        // If propertiesForLang is still null here, it means none of the fallback languages were successfully loaded.
        if (properties == null) {
            return key
        }

        // Get the string pattern for the selected language
        val pattern = properties.getProperty(key) ?: return key

        return try {
            if (args.isNotEmpty()) {
                // If arguments are provided (the args array is not null and not empty), attempt to format the string.
                MessageFormat.format(pattern, *args)
            } else {
                // If no arguments were provided to the vararg, just return the raw pattern string.
                pattern
            }
        } catch (_: IllegalArgumentException) {
            // Handle potential formatting errors (e.g., incorrect number/type of args for placeholders).
            println("Failed to format localization key '$key' with arguments: ${args.joinToString()}")
            return pattern
        } catch (e: Exception) {
            // Catch any other unexpected exceptions during formatting.
            println("An unexpected error occurred while formatting localization with arguments: " +
                    "${args.joinToString()} - ${e.message}")
            return pattern
        }
    }

    override fun getConsole(key: String, vararg args: Any?): String {
        return get(config.pluginLanguage, key, *args)
    }

    // Private function to handle the layered loading process
    private fun loadLayeredProperties() {
        val langFolder = File(dataFolder, "lang")
        val defaultsFolder = File(langFolder, "defaults")
        val overridesFolder = File(langFolder, "overrides")

        // Find all language codes present in the defaults and overrides folders
        val availableLanguages = findAvailableLanguages(defaultsFolder, overridesFolder)

        availableLanguages.forEach { locale ->
            val properties = Properties()

            // Layer 1: If the requested language is different from base, load its default version
            val setLanguage = config.pluginLanguage
            if (setLanguage != baseDefaultLanguageCode) {
                val specificDefaultFile = File(defaultsFolder, "$setLanguage.properties")
                if (specificDefaultFile.exists()) {
                    try {
                        specificDefaultFile.inputStream().use { properties.load(it) }
                        println("Loaded language: $setLanguage")
                    } catch (e: Exception) {
                        println("Failed to load default language file for $setLanguage")
                    }
                }
            }

            // Layer 2: Load the override language file
            val overrideFile = File(overridesFolder, "$setLanguage.properties")
            if (overrideFile.exists()) {
                try {
                    overrideFile.inputStream().use { properties.load(it) }
                    println("Loaded override language file: $setLanguage")
                } catch (e: Exception) {
                    println("Failed to load override language file for $setLanguage")
                }
            }

            languages[locale] = properties
        }
    }

    private fun findAvailableLanguages(defaultsFolder: File, overridesFolder: File): Set<String> {
        val codes = mutableSetOf<String>()

        // Scan defaults folder for .properties files
        defaultsFolder.listFiles { file -> file.isFile && file.extension == "properties" }?.forEach { file ->
            codes.add(file.nameWithoutExtension)
        }

        // Scan overrides folder for .properties files
        // Overrides might introduce new language codes or just override existing ones
        overridesFolder.listFiles { file -> file.isFile && file.extension == "properties" }?.forEach { file ->
            codes.add(file.nameWithoutExtension)
        }

        return codes
    }
}