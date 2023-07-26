package dev.mizarc.bellclaims.utils

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import dev.mizarc.bellclaims.listeners.ClaimRule

fun ClaimRule.getIcon(): ItemStack {
    return when (this) {
        ClaimRule.Explosions -> ItemStack(Material.TNT)
        ClaimRule.FireSpread -> ItemStack(Material.FLINT_AND_STEEL)
        ClaimRule.MobGriefing -> ItemStack(Material.CREEPER_HEAD)
        ClaimRule.Pistons -> ItemStack(Material.PISTON)
    }
}

fun ClaimRule.getDisplayName(): String {
    return when (this) {
        ClaimRule.Explosions -> "Explosions"
        ClaimRule.FireSpread -> "Fire Spread"
        ClaimRule.MobGriefing -> "Mob Griefing"
        ClaimRule.Pistons -> "Pistons"
    }
}

fun ClaimRule.getDescription(): String {
    return when (this) {
        ClaimRule.Explosions -> "Allows TNT to damage claim blocks"
        ClaimRule.FireSpread -> "Allows fire to spread to other blocks"
        ClaimRule.MobGriefing -> "Allows mobs to damage claim blocks"
        ClaimRule.Pistons -> "Allows pistons to move claim blocks"
    }
}