package dev.mizarc.bellclaims

import co.aikar.commands.PaperCommandManager
import dev.mizarc.bellclaims.di.appModule
import dev.mizarc.bellclaims.infrastructure.persistence.Config
import dev.mizarc.bellclaims.interaction.commands.*
import dev.mizarc.bellclaims.interaction.listeners.*
import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitScheduler
import org.koin.core.context.GlobalContext.startKoin
import java.io.File


/**
 * The entry point for the Bell Claims plugin.
 */
class BellClaims : JavaPlugin() {
    private lateinit var commandManager: PaperCommandManager
    lateinit var metadata: Chat
    internal var conf: Config = Config(this)
    private lateinit var scheduler: BukkitScheduler

    companion object {
        lateinit var instance: BellClaims
    }
    private lateinit var langConfig: FileConfiguration
    private lateinit var langFile: File

    override fun onEnable() {
        scheduler = server.scheduler
        startKoin { modules(appModule(this@BellClaims)) }
        initLang()
        initialiseVaultDependency()
        commandManager = PaperCommandManager(this)
        registerCommands()
        registerEvents()

//NOTE: new language functions
//I don't know where to put them
        InitLangConfig()
        loadLangConfig()


        logger.info("Bell Claims has been Enabled")
    }

    fun initLang() {
        val defaultLanguageFilenames = listOf(
            "en.properties",
            "fr.properties",
            "es.properties"
        )

        // Move languages to the required folder and add readme for override instructions
        defaultLanguageFilenames.forEach { filename ->
            val resourcePathInJar = "lang/defaults/$filename"
            saveResource(resourcePathInJar, true)
        }
        saveResource("lang/overrides/README.txt", true)
    }
    
//---Maybe Put It Somewhere else?---//

    /**
     * Initializes lang system.
     */
    fun InitLangConfig() {
        instance = this
        saveDefaultConfig()
        var selectedLanguage = conf.pluginLanguage
        val langFileName = "lang_${selectedLanguage}.yml"
        langFile = File(dataFolder, langFileName)
        if (!langFile.exists()) {
            saveResource(langFileName, false)
        }
    }

    /**
     * Load lang config.
     */
    fun loadLangConfig() {
        try {
            langConfig = YamlConfiguration.loadConfiguration(langFile)
        } catch (e: IllegalArgumentException) {
            langConfig = YamlConfiguration()
        }
    }

    /**
     * apiFunction for getLangText() in utils.
     */
    fun getText(key: String): String {
    return langConfig.getString(key, "String key not found") ?: "String key not found"
    }

//---------------------------------------

    override fun onDisable() {
        logger.info("Bell Claims has been Disabled")
    }

    private fun initialiseVaultDependency() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            server.servicesManager.getRegistration(Chat::class.java)?.let { metadata = it.provider }
            logger.info(Chat::class.java.toString())
        }
    }

    /**
     * Registers all commands.
     */
    private fun registerCommands() {
        commandManager.registerCommand(ClaimListCommand())
        commandManager.registerCommand(ClaimCommand())
        commandManager.registerCommand(InfoCommand())
        commandManager.registerCommand(RenameCommand())
        commandManager.registerCommand(DescriptionCommand())
        commandManager.registerCommand(PartitionsCommand())
        commandManager.registerCommand(AddFlagCommand())
        commandManager.registerCommand(RemoveFlagCommand())
        commandManager.registerCommand(TrustListCommand())
        commandManager.registerCommand(TrustCommand())
        commandManager.registerCommand(TrustAllCommand())
        commandManager.registerCommand(UntrustCommand())
        commandManager.registerCommand(UntrustAllCommand())
        commandManager.registerCommand(RemoveCommand())
        commandManager.registerCommand(ClaimOverrideCommand())
        commandManager.registerCommand(ClaimMenuCommand())
    }

    /**
     * Registers all listeners.
     */
    private fun registerEvents() {
        server.pluginManager.registerEvents(BlockLaunchListener(this), this)
        server.pluginManager.registerEvents(ClaimAnchorListener(), this)
        server.pluginManager.registerEvents(ClaimDestructionListener(), this)
        server.pluginManager.registerEvents(CloseInventoryListener(), this)
        server.pluginManager.registerEvents(EditToolListener(), this)
        server.pluginManager.registerEvents(EditToolVisualisingListener(this), this)
        server.pluginManager.registerEvents(HarvestReplantListener(), this)
        server.pluginManager.registerEvents(MoveToolListener(), this)
        server.pluginManager.registerEvents(PartitionUpdateListener(), this)
        server.pluginManager.registerEvents(PlayerClaimProtectionListener(), this)
        server.pluginManager.registerEvents(ToolRemovalListener(), this)
        server.pluginManager.registerEvents(WorldClaimProtectionListener(), this)
    }
}
