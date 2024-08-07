package dev.mizarc.bellclaims

import co.aikar.commands.PaperCommandManager
import dev.mizarc.bellclaims.api.*
import dev.mizarc.bellclaims.domain.claims.ClaimRepository
import dev.mizarc.bellclaims.domain.permissions.ClaimPermissionRepository
import dev.mizarc.bellclaims.domain.flags.ClaimFlagRepository
import dev.mizarc.bellclaims.domain.permissions.PlayerAccessRepository
import dev.mizarc.bellclaims.domain.partitions.PartitionRepository
import dev.mizarc.bellclaims.domain.players.PlayerStateRepository
import net.milkbowl.vault.chat.Chat
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import dev.mizarc.bellclaims.infrastructure.persistence.Config
import dev.mizarc.bellclaims.infrastructure.persistence.claims.ClaimRepositorySQLite
import dev.mizarc.bellclaims.infrastructure.persistence.partitions.PartitionRepositorySQLite
import dev.mizarc.bellclaims.infrastructure.persistence.players.PlayerStateRepositoryMemory
import dev.mizarc.bellclaims.infrastructure.persistence.storage.SQLiteStorage
import dev.mizarc.bellclaims.infrastructure.persistence.claims.ClaimFlagRepositorySQLite
import dev.mizarc.bellclaims.infrastructure.persistence.claims.ClaimPermissionRepositorySQLite
import dev.mizarc.bellclaims.infrastructure.persistence.claims.PlayerAccessRepositorySQLite
import dev.mizarc.bellclaims.infrastructure.services.*
import dev.mizarc.bellclaims.infrastructure.services.playerlimit.SimplePlayerLimitServiceImpl
import dev.mizarc.bellclaims.infrastructure.services.playerlimit.VaultPlayerLimitServiceImpl
import dev.mizarc.bellclaims.interaction.commands.*
import dev.mizarc.bellclaims.interaction.listeners.*
import dev.mizarc.bellclaims.interaction.visualisation.Visualiser
import org.bukkit.Bukkit

/**
 * The entry point for the Bell Claims plugin.
 */
class BellClaims : JavaPlugin() {
    private lateinit var commandManager: PaperCommandManager
    private lateinit var metadata: Chat
    internal var config: Config = Config(this)
    val storage = SQLiteStorage(this)

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

    override fun onEnable() {
        initialiseVaultDependency()
        initialiseRepositories()
        initialiseServices()
        initialiseInteractions()

        commandManager = PaperCommandManager(this)
        registerDependencies()
        registerCommands()
        registerEvents()

        logger.info("Bell Claims has been Enabled")
    }

    override fun onDisable() {
        logger.info("Bell Claims has been Disabled")
    }

    private fun initialiseVaultDependency() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            val serviceProvider: RegisteredServiceProvider<Chat> = server.servicesManager
                .getRegistration(Chat::class.java)!!
            metadata = serviceProvider.provider
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
        claimWorldService = ClaimWorldServiceImpl(claimRepo, partitionService, playerLimitService)
        flagService = FlagServiceImpl(claimFlagRepo)
        defaultPermissionService = DefaultPermissionServiceImpl(claimPermissionRepo)
        playerPermissionService = PlayerPermissionServiceImpl(playerAccessRepo)
        visualisationService = VisualisationServiceImpl(partitionService)
    }

    /**
     * Initialises all special interactions.
     */
    private fun initialiseInteractions() {
        visualiser = Visualiser(this, claimService, partitionService, playerStateService, visualisationService)
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
                visualiser), this)
        server.pluginManager.registerEvents(
            EditToolVisualisingListener(this, playerStateService, visualiser), this)
        server.pluginManager.registerEvents(PlayerRegistrationListener(playerStateService), this)
        server.pluginManager.registerEvents(EditToolRemovalListener(), this)
        server.pluginManager.registerEvents(ClaimBellListener(claimService, claimWorldService, flagService,
            defaultPermissionService, playerPermissionService, playerLimitService), this)
        server.pluginManager.registerEvents(ClaimDestructionListener(claimService, claimWorldService,
            playerStateService), this)
        server.pluginManager.registerEvents(MoveToolListener(claimRepo, partitionService), this)
        server.pluginManager.registerEvents(MoveToolRemovalListener(), this)
        server.pluginManager.registerEvents(MiscPreventionsListener(claimService, partitionService), this)
        server.pluginManager.registerEvents(
            Visualiser(this, claimService, partitionService,
                playerStateService, visualisationService), this)
        server.pluginManager.registerEvents(
            PartitionUpdateListener(claimService, partitionService, playerStateService, visualiser), this)
    }
}
