package dev.mizarc.bellclaims

import co.aikar.commands.PaperCommandManager
import dev.mizarc.bellclaims.api.*
import dev.mizarc.bellclaims.domain.claims.ClaimRepository
import dev.mizarc.bellclaims.domain.flags.ClaimFlagRepository
import dev.mizarc.bellclaims.domain.partitions.PartitionRepository
import dev.mizarc.bellclaims.domain.permissions.ClaimPermissionRepository
import dev.mizarc.bellclaims.domain.permissions.PlayerAccessRepository
import dev.mizarc.bellclaims.domain.players.PlayerStateRepository
import dev.mizarc.bellclaims.infrastructure.persistence.Config
import dev.mizarc.bellclaims.infrastructure.persistence.claims.ClaimFlagRepositorySQLite
import dev.mizarc.bellclaims.infrastructure.persistence.claims.ClaimPermissionRepositorySQLite
import dev.mizarc.bellclaims.infrastructure.persistence.claims.ClaimRepositorySQLite
import dev.mizarc.bellclaims.infrastructure.persistence.claims.PlayerAccessRepositorySQLite
import dev.mizarc.bellclaims.infrastructure.persistence.partitions.PartitionRepositorySQLite
import dev.mizarc.bellclaims.infrastructure.persistence.players.PlayerStateRepositoryMemory
import dev.mizarc.bellclaims.infrastructure.persistence.storage.SQLiteStorage
import dev.mizarc.bellclaims.infrastructure.services.*
import dev.mizarc.bellclaims.infrastructure.services.playerlimit.SimplePlayerLimitServiceImpl
import dev.mizarc.bellclaims.infrastructure.services.playerlimit.VaultPlayerLimitServiceImpl
import dev.mizarc.bellclaims.interaction.commands.*
import dev.mizarc.bellclaims.interaction.listeners.*
import dev.mizarc.bellclaims.interaction.visualisation.Visualiser
import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitScheduler
import java.io.File


/**
 * The entry point for the Bell Claims plugin.
 */
class BellClaims : JavaPlugin() {
    private lateinit var commandManager: PaperCommandManager
    private lateinit var metadata: Chat
    internal var config: Config = Config(this)
    val storage = SQLiteStorage(this)
    private lateinit var scheduler: BukkitScheduler

    private lateinit var claimRepo: ClaimRepository
    private lateinit var partitionRepo: PartitionRepository
    private lateinit var claimFlagRepo: ClaimFlagRepository
    private lateinit var claimPermissionRepo: ClaimPermissionRepository
    private lateinit var playerAccessRepo: PlayerAccessRepository
    private lateinit var playerStateRepo: PlayerStateRepository

    private lateinit var playerLimitService: PlayerLimitService
    private lateinit var playerStateService: PlayerStateService
    private lateinit var claimService: ClaimService
    private lateinit var partitionService: PartitionService
    private lateinit var claimWorldService: ClaimWorldService
    private lateinit var flagService: FlagService
    private lateinit var defaultPermissionService: DefaultPermissionService
    private lateinit var playerPermissionService: PlayerPermissionService
    private lateinit var visualisationService: VisualisationService

    private lateinit var visualiser: Visualiser

    companion object {
        lateinit var instance: BellClaims
    }
    private lateinit var langConfig: FileConfiguration
    private lateinit var langFile: File

    override fun onEnable() {
        scheduler = server.scheduler

        initialiseVaultDependency()
        initialiseRepositories()
        initialiseServices()
        initialiseInteractions()

        commandManager = PaperCommandManager(this)
        registerDependencies()
        registerCommands()
        registerEvents()

//NOTE: new language functions
//I don't know where to put them
        InitLangConfig()
        loadLangConfig()

        logger.info("Bell Claims has been Enabled")
    }
    
//---Maybe Put It Somewhere else?---//

    /**
     * Initializes lang system.
     */
    fun InitLangConfig() {
        instance = this
        saveDefaultConfig()
        var selectedLanguage = config.pluginLanguage
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
     * Initialises all repositories.
     */
    private fun initialiseRepositories() {
        claimRepo = ClaimRepositorySQLite(storage)
        partitionRepo = PartitionRepositorySQLite(storage)
        claimFlagRepo = ClaimFlagRepositorySQLite(storage)
        claimPermissionRepo = ClaimPermissionRepositorySQLite(storage)
        playerAccessRepo = PlayerAccessRepositorySQLite(storage)
        playerStateRepo = PlayerStateRepositoryMemory()
    }

    /**
     * Initialises all services.
     */
    private fun initialiseServices() {
        playerLimitService = if (::metadata.isInitialized) {
            VaultPlayerLimitServiceImpl(config, metadata, claimRepo, partitionRepo)
        } else {
            SimplePlayerLimitServiceImpl(config, claimRepo, partitionRepo)
        }

        playerStateService = PlayerStateServiceImpl(playerStateRepo)
        claimService = ClaimServiceImpl(claimRepo, partitionRepo, claimFlagRepo, claimPermissionRepo, playerAccessRepo)
        partitionService = PartitionServiceImpl(config, partitionRepo, claimService, playerLimitService)
        claimWorldService = ClaimWorldServiceImpl(claimRepo, partitionService, playerLimitService, config)
        flagService = FlagServiceImpl(claimFlagRepo)
        defaultPermissionService = DefaultPermissionServiceImpl(claimPermissionRepo)
        playerPermissionService = PlayerPermissionServiceImpl(playerAccessRepo)
        visualisationService = VisualisationServiceImpl(partitionService)
    }

    /**
     * Initialises all special interactions.
     */
    private fun initialiseInteractions() {
        visualiser = Visualiser(this, claimService, partitionService, playerStateService,
            visualisationService, config)
    }

    /**
     * Registers all dependencies to be automatically captured by the command framework.
     */
    private fun registerDependencies() {
        commandManager.registerDependency(PlayerLimitService::class.java, playerLimitService)
        commandManager.registerDependency(PlayerStateService::class.java, playerStateService)
        commandManager.registerDependency(ClaimService::class.java, claimService)
        commandManager.registerDependency(PartitionService::class.java, partitionService)
        commandManager.registerDependency(ClaimWorldService::class.java, claimWorldService)
        commandManager.registerDependency(FlagService::class.java, flagService)
        commandManager.registerDependency(DefaultPermissionService::class.java, defaultPermissionService)
        commandManager.registerDependency(PlayerPermissionService::class.java, playerPermissionService)
        commandManager.registerDependency(VisualisationService::class.java, visualisationService)
        commandManager.registerDependency(Visualiser::class.java, visualiser)
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
        server.pluginManager.registerEvents(
            ClaimInteractListener(this, claimService, partitionService, flagService, defaultPermissionService,
                playerPermissionService, playerStateService), this)
        server.pluginManager.registerEvents(
            EditToolListener(claimRepo, partitionService, playerLimitService, playerStateService, claimService,
                visualiser, config), this)
        server.pluginManager.registerEvents(
            EditToolVisualisingListener(this, playerStateService, visualiser, config), this)
        server.pluginManager.registerEvents(PlayerRegistrationListener(playerStateService), this)
        server.pluginManager.registerEvents(PlayerStateListener(playerStateService), this)
        server.pluginManager.registerEvents(ToolRemovalListener(), this)
        server.pluginManager.registerEvents(ClaimBellListener(claimService, claimWorldService, flagService,
            defaultPermissionService, playerPermissionService, playerLimitService, playerStateService), this)
        server.pluginManager.registerEvents(ClaimDestructionListener(claimService, claimWorldService,
            playerStateService), this)
        server.pluginManager.registerEvents(MoveToolListener(claimRepo, partitionService), this)
        server.pluginManager.registerEvents(
            Visualiser(this, claimService, partitionService,
                playerStateService, visualisationService, config), this)
        server.pluginManager.registerEvents(
            PartitionUpdateListener(claimService, partitionService, playerStateService, visualiser), this)
        server.pluginManager.registerEvents(BlockLaunchListener(this), this)
    }
}
