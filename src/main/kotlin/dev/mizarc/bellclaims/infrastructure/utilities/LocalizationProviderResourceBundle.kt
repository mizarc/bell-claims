package dev.mizarc.bellclaims.infrastructure.utilities

import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import dev.mizarc.bellclaims.config.MainConfig
import java.io.File
import java.text.MessageFormat
import java.util.*

class LocalizationProviderResourceBundle(private val config: MainConfig,
                                         private val dataFolder: File): LocalizationProvider {
    private val languages: MutableMap<String, Properties> = mutableMapOf()
    private val baseDefaultLanguageCode = "en"

    init {
        loadLayeredProperties()
    }

    override fun get(key: String, vararg args: Any?): String {
        return get(config.pluginLanguage, key, *args)
    }

    override fun get(locale: String, key: String, vararg args: Any?): String {
        // --- Get the raw pattern string ---
        // Try to get the properties for the requested language code from the loaded languages.
        val properties = languages[locale]
        // If the requested language is not loaded, fallback to the server's configured default language.
            ?: languages[config.pluginLanguage]
            // If the server default is also not loaded, fallback to the base default language ("en").
            ?: languages[baseDefaultLanguageCode]
            // If even the base default is missing (a severe issue), return a critical error message.
            ?: run {
                println("Localization properties for language code '$locale', server default " +
                        "'${config.pluginLanguage}', and base default '$baseDefaultLanguageCode' not found!")
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

    // Private function to handle the layered loading process
    private fun loadLayeredProperties() {
        val langFolder = File(dataFolder, "lang")
        val defaultsFolder = File(langFolder, "defaults")
        val overridesFolder = File(langFolder, "overrides")

        // Find all language codes present in the defaults and overrides folders
        val availableLanguages = findAvailableLanguages(defaultsFolder, overridesFolder)

        // Layer 1: Load the base default language (e.g., en.properties from defaults)
        availableLanguages.forEach { locale ->
            val properties = Properties()
            val baseDefaultFile = File(defaultsFolder, "$baseDefaultLanguageCode.properties")
            if (baseDefaultFile.exists()) {
                try {
                    baseDefaultFile.inputStream().use { properties.load(it) }
                    println("Loaded base default language: $baseDefaultLanguageCode")
                } catch (e: Exception) {
                    println(
                        "Failed to load base default language file " +
                                "'lang/defaults/$baseDefaultLanguageCode.properties': ${e.message}"
                    )
                }
            } else {
                println(
                    "Base default language file 'lang/defaults/$baseDefaultLanguageCode.properties' not found! " +
                            "Using default messages (keys)."
                )
            }

            // Layer 2: If the requested language is different from base, load its default version
            val setLanguage = config.pluginLanguage
            if (setLanguage != baseDefaultLanguageCode) {
                val specificDefaultFile = File(defaultsFolder, "$setLanguage.properties")
                if (specificDefaultFile.exists()) {
                    try {
                        specificDefaultFile.inputStream().use { properties.load(it) }
                        println("Loaded specific default language: $setLanguage")
                    } catch (e: Exception) {
                        println(
                            "Failed to load specific default language file " +
                                    "'lang/defaults/$setLanguage.properties': ${e.message}"
                        )
                    }
                } else {
                    println(
                        "Specific default language file 'lang/defaults/$setLanguage.properties' not found for " +
                                "'$setLanguage'. Falling back to base default."
                    )
                }
            }

            // Layer 3: Load the override language file
            val overrideFile = File(overridesFolder, "$setLanguage.properties")
            if (overrideFile.exists()) {
                try {
                    overrideFile.inputStream().use { properties.load(it) }
                    println("Loaded override language file: $setLanguage")
                } catch (e: Exception) {
                    println("Failed to load override language file 'lang/overrides/$setLanguage.properties': " +
                            "${e.message}")
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