package dev.mizarc.bellclaims.utils

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import dev.mizarc.bellclaims.interaction.listeners.Flag

fun Flag.getIcon(): ItemStack {
    return when (this) {
        Flag.Explosions -> ItemStack(Material.TNT)
        Flag.FireSpread -> ItemStack(Material.FLINT_AND_STEEL)
        Flag.MobGriefing -> ItemStack(Material.CREEPER_HEAD)
        Flag.Pistons -> ItemStack(Material.PISTON)
    }
}

fun Flag.getDisplayName(): String {
    return when (this) {
        Flag.Explosions -> "Explosions"
        Flag.FireSpread -> "Fire Spread"
        Flag.MobGriefing -> "Mob Griefing"
        Flag.Pistons -> "Pistons"
    }
}

fun Flag.getDescription(): String {
    return when (this) {
        Flag.Explosions -> "Allows TNT to damage claim blocks"
        Flag.FireSpread -> "Allows fire to spread to other blocks"
        Flag.MobGriefing -> "Allows mobs to damage claim blocks"
        Flag.Pistons -> "Allows pistons to move claim blocks"
    }
}