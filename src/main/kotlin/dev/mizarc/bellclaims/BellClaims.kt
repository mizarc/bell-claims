package dev.mizarc.bellclaims

import co.aikar.commands.PaperCommandManager
import dev.mizarc.bellclaims.api.*
import dev.mizarc.bellclaims.domain.claims.ClaimRepository
import dev.mizarc.bellclaims.domain.claims.ClaimPermissionRepository
import dev.mizarc.bellclaims.domain.claims.ClaimFlagRepository
import dev.mizarc.bellclaims.domain.claims.PlayerAccessRepository
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
import dev.mizarc.bellclaims.infrastructure.persistence.claims.PlayerAccessRepositorySQLite
import dev.mizarc.bellclaims.infrastructure.services.*
import dev.mizarc.bellclaims.interaction.commands.*
import dev.mizarc.bellclaims.interaction.listeners.*

class BellClaims : JavaPlugin() {
    private lateinit var commandManager: PaperCommandManager
    private lateinit var metadata: Chat
    internal var config: Config = Config(this)
    val storage = SQLiteStorage(this)
    private lateinit var claimRepo: ClaimRepository
    private lateinit var partitionRepo: PartitionRepository
    private lateinit var claimPermissionRepo: ClaimPermissionRepository
    private lateinit var claimRuleRepo: ClaimFlagRepository
    private lateinit var playerAccessRepo: PlayerAccessRepository
    private lateinit var playerStateRepo: PlayerStateRepository

    private lateinit var playerStateService: PlayerStateService
    private lateinit var claimService: ClaimService
    private lateinit var partitionService: PartitionService
    private lateinit var claimWorldService: ClaimWorldService
    private lateinit var flagService: FlagService
    private lateinit var defaultPermissionService: DefaultPermissionService
    private lateinit var playerPermissionService: PlayerPermissionService
    private lateinit var visualisationService: VisualisationService

    private val visualiser = Visualiser(this, claimService, partitionService, playerStateRepo)

    override fun onEnable() {
        logger.info(Chat::class.java.toString())
        initialiseRepositories()
        initialiseServices()
        val serviceProvider: RegisteredServiceProvider<Chat> = server.servicesManager
            .getRegistration(Chat::class.java)!!
        commandManager = PaperCommandManager(this)
        metadata = serviceProvider.provider
        registerDependencies()
        registerCommands()
        registerEvents()
        logger.info("Bell Claims has been Enabled")
    }

    override fun onDisable() {
        logger.info("Bell Claims has been Disabled")
    }

    private fun initialiseRepositories() {
        claimRepo = ClaimRepositorySQLite(storage)
        partitionRepo = PartitionRepositorySQLite(storage)
        claimRuleRepo = ClaimFlagRepositorySQLite(storage)
        playerAccessRepo = PlayerAccessRepositorySQLite(storage)
        playerStateRepo = PlayerStateRepositoryMemory()
    }

    private fun initialiseServices() {
        playerStateService = PlayerStateServiceImpl(config, metadata, playerStateRepo, claimRepo, partitionRepo)
        claimService = ClaimServiceImpl(claimRepo, partitionRepo, claimRuleRepo, claimPermissionRepo, playerAccessRepo)
        partitionService = PartitionServiceImpl(config, partitionRepo, claimService, playerStateService)
        claimWorldService = ClaimWorldServiceImpl(claimRepo, partitionService, playerStateService)
        defaultPermissionService = DefaultPermissionServiceImpl(claimPermissionRepo)
        playerPermissionService = PlayerPermissionServiceImpl(playerAccessRepo)
    }

    private fun registerDependencies() {
        commandManager.registerDependency(Visualiser::class.java, visualiser)
        commandManager.registerDependency(ClaimService::class.java, claimService)
        commandManager.registerDependency(ClaimWorldService::class.java, claimWorldService)
        commandManager.registerDependency(PartitionService::class.java, partitionService)
        commandManager.registerDependency(DefaultPermissionService::class.java, defaultPermissionService)
        commandManager.registerDependency(PlayerPermissionService::class.java, playerPermissionService)
        commandManager.registerDependency(VisualisationService::class.java, visualisationService)
    }

    private fun registerCommands() {
        commandManager.registerCommand(ClaimCommand())
        commandManager.registerCommand(ClaimlistCommand())
        commandManager.registerCommand(TrustCommand())
        commandManager.registerCommand(PartitionlistCommand())
        commandManager.registerCommand(TrustlistCommand())
        commandManager.registerCommand(InfoCommand())
        commandManager.registerCommand(UntrustCommand())
        commandManager.registerCommand(RenameCommand())
        commandManager.registerCommand(DescriptionCommand())
        commandManager.registerCommand(AddRuleCommand())
        commandManager.registerCommand(RemoveRuleCommand())
        commandManager.registerCommand(ClaimOverrideCommand())
        commandManager.registerCommand(ClaimMenuCommand())
    }

    private fun registerEvents() {
        server.pluginManager.registerEvents(
            ClaimInteractListener(this, claimService, partitionService, flagService, defaultPermissionService,
                playerPermissionService, playerStateService), this)
        server.pluginManager.registerEvents(
            EditToolListener(claimRepo, partitionService, playerStateService, claimService, visualiser), this)
        server.pluginManager.registerEvents(Visualiser(this, claimService, partitionService, playerStateRepo),
            this)
        server.pluginManager.registerEvents(PlayerRegistrationListener(playerStateService), this)
        server.pluginManager.registerEvents(ClaimToolRemovalListener(), this)
        server.pluginManager.registerEvents(ClaimBellListener(claimService, claimWorldService, flagService,
            defaultPermissionService, playerPermissionService, playerStateService), this)
        server.pluginManager.registerEvents(ClaimDestructionListener(claimService, claimWorldService), this)
        server.pluginManager.registerEvents(ClaimMoveListener(claimRepo, partitionService), this)
        server.pluginManager.registerEvents(ClaimMoveToolRemovalListener(), this)
        server.pluginManager.registerEvents(MiscPreventions(claimService, partitionService), this)
    }
}
