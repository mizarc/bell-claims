package dev.mizarc.bellclaims

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.Default
import dev.mizarc.bellclaims.api.*
import dev.mizarc.bellclaims.domain.claims.ClaimRepository
import dev.mizarc.bellclaims.domain.claims.ClaimPermissionRepository
import dev.mizarc.bellclaims.domain.claims.ClaimRuleRepository
import dev.mizarc.bellclaims.domain.claims.PlayerAccessRepository
import dev.mizarc.bellclaims.domain.partitions.PartitionRepository
import dev.mizarc.bellclaims.domain.players.PlayerStateRepository
import net.milkbowl.vault.chat.Chat
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import dev.mizarc.bellclaims.infrastructure.persistence.Config
import dev.mizarc.bellclaims.infrastructure.persistence.claims.ClaimRepositorySQLite
import dev.mizarc.bellclaims.infrastructure.services.ClaimServiceImpl
import dev.mizarc.bellclaims.infrastructure.persistence.partitions.PartitionRepositorySQLite
import dev.mizarc.bellclaims.infrastructure.persistence.players.PlayerStateRepositoryMemory
import dev.mizarc.bellclaims.infrastructure.persistence.DatabaseStorage
import dev.mizarc.bellclaims.infrastructure.persistence.claims.ClaimRuleRepositorySQLite
import dev.mizarc.bellclaims.infrastructure.persistence.claims.PlayerAccessRepositorySQLite
import dev.mizarc.bellclaims.infrastructure.services.PartitionServiceImpl
import dev.mizarc.bellclaims.interaction.commands.*
import dev.mizarc.bellclaims.interaction.listeners.*

class BellClaims : JavaPlugin() {
    private lateinit var commandManager: PaperCommandManager
    private lateinit var metadata: Chat
    internal var config: Config = Config(this)
    val storage = DatabaseStorage(this)
    private lateinit var claimRepo: ClaimRepository
    private lateinit var partitionRepo: PartitionRepository
    private lateinit var claimPermissionRepo: ClaimPermissionRepository
    private lateinit var claimRuleRepo: ClaimRuleRepository
    private lateinit var playerAccessRepo: PlayerAccessRepository
    private lateinit var playerStateRepo: PlayerStateRepository
    private lateinit var claimService: ClaimService
    private lateinit var partitionService: PartitionService
    private lateinit var claimPermissionService: DefaultPermissionService
    private lateinit var playerPermissionService: PlayerPermissionService
    private lateinit var visualisationService: VisualisationService
    private lateinit var playerStateService: PlayerStateService
    val claimVisualiser = ClaimVisualiser(this, claimService, partitionService, playerStateRepo)

    override fun onEnable() {
        // Initialise Repositories
        claimRepo = ClaimRepositorySQLite(storage)
        partitionRepo = PartitionRepositorySQLite(storage)
        claimRuleRepo = ClaimRuleRepositorySQLite(storage)
        playerAccessRepo = PlayerAccessRepositorySQLite(storage)
        playerStateRepo = PlayerStateRepositoryMemory()

        // Initialise Services
        claimService = ClaimServiceImpl(claimRepo, partitionRepo, claimRuleRepo,
            claimPermissionRepo, partitionService, playerStateService)
        partitionService = PartitionServiceImpl(config, claimService, partitionRepo)

        logger.info(Chat::class.java.toString())
        val serviceProvider: RegisteredServiceProvider<Chat> = server.servicesManager.getRegistration(Chat::class.java)!!
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

    private fun registerDependencies() {
        commandManager.registerDependency(ClaimRepositorySQLite::class.java, claimRepo)
        commandManager.registerDependency(PartitionRepository::class.java, partitionRepo)
        commandManager.registerDependency(ClaimRuleRepository::class.java, claimRuleRepo)
        commandManager.registerDependency(ClaimPermissionRepository::class.java, claimPermissionRepo)
        commandManager.registerDependency(PlayerAccessRepository::class.java, playerAccessRepo)
        commandManager.registerDependency(PlayerStateRepositoryMemory::class.java, playerStateRepo)
        commandManager.registerDependency(ClaimVisualiser::class.java, claimVisualiser)
        commandManager.registerDependency(ClaimService::class.java, claimService)
        commandManager.registerDependency(PartitionService::class.java, partitionService)
    }

    private fun registerCommands() {
        commandManager.registerCommand(ClaimCommand())
        commandManager.registerCommand(ClaimlistCommand())
        commandManager.registerCommand(UnclaimCommand())
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
            ClaimEventHandler(this, claimRepo, partitionRepo,
            claimRuleRepo, claimPermissionRepo, playerAccessRepo, playerStateRepo, claimService, partitionService),
            this)
        server.pluginManager.registerEvents(
            ClaimToolListener(claimRepo, playerStateRepo, claimService,
            partitionService, claimVisualiser), this)
        server.pluginManager.registerEvents(ClaimVisualiser(this, claimService, partitionService, playerStateRepo), this)
        server.pluginManager.registerEvents(
            PlayerRegistrationListener(config, metadata,
            playerStateRepo), this)
        server.pluginManager.registerEvents(ClaimToolRemovalListener(), this)
        server.pluginManager.registerEvents(
            ClaimManagementListener(claimRepo, partitionRepo,
            claimRuleRepo, claimPermissionRepo, playerAccessRepo, partitionService, claimService), this)
        server.pluginManager.registerEvents(ClaimDestructionListener(claimService, claimVisualiser), this)
        server.pluginManager.registerEvents(ClaimMoveListener(claimRepo, partitionService), this)
        server.pluginManager.registerEvents(ClaimMoveToolRemovalListener(), this)
        server.pluginManager.registerEvents(MiscPreventions(claimService, partitionService), this)
    }
}
