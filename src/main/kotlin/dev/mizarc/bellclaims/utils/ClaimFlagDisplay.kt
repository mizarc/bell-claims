package dev.mizarc.bellclaims.utils

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import dev.mizarc.bellclaims.domain.flags.Flag

/**
 * Associates claim flags with a specific in-game item.
 *
 * @return ItemStack of the associated item for the given flag enum.
 */
fun Flag.getIcon(): ItemStack {
    return when (this) {
        Flag.Explosions -> ItemStack(Material.TNT)
        Flag.FireSpread -> ItemStack(Material.FLINT_AND_STEEL)
        Flag.MobGriefing -> ItemStack(Material.CREEPER_HEAD)
        Flag.Pistons -> ItemStack(Material.PISTON)
        Flag.Fluids -> ItemStack(Material.WATER_BUCKET)
    }
}

/**
 * Display names for each flag.
 *
 * @return The set display name for the given flag enum.
 */
fun Flag.getDisplayName(): String {
    return when (this) {
        Flag.Explosions -> "Explosions"
        Flag.FireSpread -> "Fire Spread"
        Flag.MobGriefing -> "Mob Griefing"
        Flag.Pistons -> "Pistons"
        Flag.Fluids -> "Fluid Flow"
    }
}

/**
 * Display descriptions for each flag.
 *
 * @return The set display description for the given flag enum.
 */
fun Flag.getDescription(): String {
    return when (this) {
        Flag.Explosions -> "Allows TNT to damage claim blocks"
        Flag.FireSpread -> "Allows fire to spread to other blocks"
        Flag.MobGriefing -> "Allows mobs to damage claim blocks"
        Flag.Pistons -> "Allows pistons to move claim blocks"
        Flag.Fluids -> "Allows fluids to flow into claim blocks"
    }
}