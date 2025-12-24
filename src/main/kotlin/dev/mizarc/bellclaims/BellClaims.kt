package dev.mizarc.bellclaims

import co.aikar.commands.PaperCommandManager
import dev.mizarc.bellclaims.di.appModule
import dev.mizarc.bellclaims.infrastructure.persistence.migrations.SQLiteMigrations
import dev.mizarc.bellclaims.interaction.commands.*
import dev.mizarc.bellclaims.interaction.listeners.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitScheduler
import org.koin.core.context.GlobalContext.startKoin
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException


/**
 * The entry point for the Bell Claims plugin.
 */
class BellClaims : JavaPlugin() {
    private lateinit var commandManager: PaperCommandManager
    lateinit var metadata: Chat
    private lateinit var scheduler: BukkitScheduler
    lateinit var pluginScope: CoroutineScope

    override fun onEnable() {
        initDataFolder()
        initDatabase()
        pluginScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scheduler = server.scheduler
        startKoin { modules(appModule(this@BellClaims)) }
        initLang()
        initialiseVaultDependency()
        initialiseConfig()
        commandManager = PaperCommandManager(this)
        registerCommands()
        registerEvents()

        logger.info("Bell Claims has been Enabled")
    }

    fun initDataFolder() {
        if (!dataFolder.exists()) {
            logger.info("Data folder '${dataFolder.absolutePath}' not found. Creating it...")
            try {
                dataFolder.mkdirs() // Create the directory and any necessary but nonexistent parent directories.
                logger.info("Data folder created successfully.")
            } catch (e: SecurityException) {
                logger.severe("Failed to create data folder '${dataFolder.absolutePath}': ${e.message}")
                e.printStackTrace()
                return
            }
        }
    }


    fun initDatabase() {
        val databaseFile = File(dataFolder, "claims.db")
        if (databaseFile.exists()) {
            var tempConnectionForMigration: Connection? = null
            try {
                tempConnectionForMigration = DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}")
                val migrator = SQLiteMigrations(this, tempConnectionForMigration)
                migrator.migrate()
            } finally {
                tempConnectionForMigration?.let {
                    try {
                        if (!it.isClosed) {
                            it.close()
                            logger.info("Closed temporary connection after migration.")
                        }
                    } catch (e: SQLException) {
                        logger.severe("Failed to close temporary database connection: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }
        } else {
            logger.info("Database file not found. Creating a new database and setting schema version to v2.")
            var newConnection: Connection? = null
            try {
                // This will create the database file if it doesn't exist
                newConnection = DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}")
                val statement = newConnection.createStatement()
                statement.execute("PRAGMA user_version = 2;")
                statement.close()
            } catch (e: SQLException) {
                logger.severe("Failed to create new database or set user_version: ${e.message}")
                e.printStackTrace()
            } finally {
                newConnection?.let {
                    try {
                        if (!it.isClosed) {
                            it.close()
                            logger.info("Closed connection for new database creation.")
                        }
                    } catch (e: SQLException) {
                        logger.severe("Failed to close new database connection: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun initLang() {
        val defaultLanguageFilenames = listOf(
            "en.properties"
        )

        // Move languages to the required folder and add readme for override instructions
        defaultLanguageFilenames.forEach { filename ->
            val resourcePathInJar = "lang/defaults/$filename"
            saveResource(resourcePathInJar, true)
        }
        saveResource("lang/overrides/README.txt", true)
    }

    override fun onDisable() {
        pluginScope.cancel()
        logger.info("Bell Claims has been Disabled")
    }

    private fun initialiseConfig() {
        saveDefaultConfig()
        getResource("config.yml")?.use { defaultConfigStream ->
            val sampleConfigFile = File(dataFolder, "sample-config.yml")
            try {
                Files.copy(defaultConfigStream, sampleConfigFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } catch (e: IOException) {
                logger.severe("Failed to copy config: ${e.message}")
            }
        } ?: logger.warning("Default config file not found in the plugin resources")
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
        server.pluginManager.registerEvents(ClaimToolAutoVisualisingListener(), this)
        server.pluginManager.registerEvents(CloseInventoryListener(), this)
        server.pluginManager.registerEvents(EditToolListener(), this)
        server.pluginManager.registerEvents(ClaimEnterListener(), this)

        val editToolVisualisingListener = EditToolVisualisingListener(this)
        server.pluginManager.registerEvents(editToolVisualisingListener, this)

        // Try to register the modern "Extra" listener only if the class exists
        try {
            Class.forName("io.papermc.paper.event.player.PlayerClientLoadedWorldEvent")
            server.pluginManager.registerEvents(EditToolVisualisingListenerExtra(editToolVisualisingListener), this)
        } catch (e: ClassNotFoundException) {
            logger.info("Skipping modern client-load events: Server version is older than 1.21.4.")
        }

        server.pluginManager.registerEvents(HarvestReplantListener(), this)
        server.pluginManager.registerEvents(MoveToolListener(), this)
        server.pluginManager.registerEvents(PartitionUpdateListener(), this)
        server.pluginManager.registerEvents(PlayerClaimProtectionListener(), this)
        server.pluginManager.registerEvents(ToolRemovalListener(), this)
        server.pluginManager.registerEvents(WorldClaimProtectionListener(), this)
    }
}
