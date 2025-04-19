package dev.mizarc.bellclaims.utils

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import dev.mizarc.bellclaims.domain.values.Flag

/**
 * Associates claim flags with a specific in-game item.
 *
 * @return ItemStack of the associated item for the given flag enum.
 */
fun Flag.getIcon(): ItemStack {
    return when (this) {
        Flag.EXPLOSIONS -> ItemStack(Material.TNT)
        Flag.FIRE_SPREAD -> ItemStack(Material.FLINT_AND_STEEL)
        Flag.MOB_GRIEFING -> ItemStack(Material.CREEPER_HEAD)
        Flag.PISTONS -> ItemStack(Material.PISTON)
        Flag.FLUIDS -> ItemStack(Material.WATER_BUCKET)
        Flag.TREES -> ItemStack(Material.OAK_SAPLING)
        Flag.SCULK -> ItemStack(Material.SCULK_CATALYST)
        Flag.DISPENSERS -> ItemStack(Material.DISPENSER)
        Flag.SPONGE -> ItemStack(Material.SPONGE)
        Flag.LIGHTNING -> ItemStack(Material.LIGHTNING_ROD)
        Flag.FALLING_BLOCK -> ItemStack(Material.ANVIL)
        Flag.ANIMAL_VEHICLE -> ItemStack(Material.OAK_BOAT)
    }
}

/**
 * Display names for each flag.
 *
 * @return The set display name for the given flag enum.
 */
fun Flag.getDisplayName(): String {
    return when (this) {
        Flag.EXPLOSIONS-> getLangText("NameFlagExplosions")
        Flag.FIRE_SPREAD -> getLangText("NameFlagFireSpread")
        Flag.MOB_GRIEFING -> getLangText("NameFlagMobGriefing")
        Flag.PISTONS -> getLangText("NameFlagPistons")
        Flag.FLUIDS -> getLangText("NameFlagFluids")
        Flag.TREES -> getLangText("NameFlagTrees")
        Flag.SCULK -> getLangText("NameFlagSculk")
        Flag.DISPENSERS -> getLangText("NameFlagDispensers")
        Flag.SPONGE -> getLangText("NameFlagSponge")
        Flag.LIGHTNING -> getLangText("NameFlagLightning")
        Flag.FALLING_BLOCK -> getLangText("NameFlagFallingBlock")
        Flag.ANIMAL_VEHICLE -> getLangText("NameFlagAnimalVehicle")
    }
}

/**
 * Display descriptions for each flag.
 *
 * @return The set display description for the given flag enum.
 */
fun Flag.getDescription(): String {
    return when (this) {
        Flag.EXPLOSIONS -> getLangText("DescFlagExplosions")
        Flag.FIRE_SPREAD -> getLangText("DescFlagFireSpread")
        Flag.MOB_GRIEFING -> getLangText("DescFlagMobGriefing")
        Flag.PISTONS -> getLangText("DescFlagPistons")
        Flag.FLUIDS -> getLangText("DescFlagFluids")
        Flag.TREES -> getLangText("DescFlagTrees")
        Flag.SCULK -> getLangText("DescFlagSculk")
        Flag.DISPENSERS -> getLangText("DescFlagDispensers")
        Flag.SPONGE -> getLangText("DescFlagSponge")
        Flag.LIGHTNING -> getLangText("DescFlagLightning")
        Flag.FALLING_BLOCK -> getLangText("DescFlagFallingBlock")
        Flag.ANIMAL_VEHICLE -> getLangText("DescFlagAnimalVehicle")
    }
}