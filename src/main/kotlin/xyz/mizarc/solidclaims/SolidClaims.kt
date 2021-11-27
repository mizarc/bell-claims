package xyz.mizarc.solidclaims

import org.bukkit.plugin.java.JavaPlugin

class SolidClaims : JavaPlugin() {
    override fun onEnable() {
        logger.info("SolidClaims has been Enabled")
    }

    override fun onDisable() {
        logger.info("SolidClaims has been Disabled")
    }
}
