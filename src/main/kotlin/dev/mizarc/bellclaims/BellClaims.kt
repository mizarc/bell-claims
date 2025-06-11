package dev.mizarc.bellclaims

import co.aikar.commands.PaperCommandManager
import dev.mizarc.bellclaims.di.appModule
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


/**
 * The entry point for the Bell Claims plugin.
 */
class BellClaims : JavaPlugin() {
    private lateinit var commandManager: PaperCommandManager
    lateinit var metadata: Chat
    private lateinit var scheduler: BukkitScheduler
    lateinit var pluginScope: CoroutineScope

    override fun onEnable() {
        pluginScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scheduler = server.scheduler
        startKoin { modules(appModule(this@BellClaims)) }
        initLang()
        initialiseVaultDependency()
        commandManager = PaperCommandManager(this)
        registerCommands()
        registerEvents()

        logger.info("Bell Claims has been Enabled")
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
