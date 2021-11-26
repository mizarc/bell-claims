package xyz.mizarc.solidclaims;

import org.bukkit.plugin.java.JavaPlugin;

public class SolidClaims extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("xyz.mizarc.solidclaims.SolidClaims has been Enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("xyz.mizarc.solidclaims.SolidClaims has been Disabled");
    }
}
