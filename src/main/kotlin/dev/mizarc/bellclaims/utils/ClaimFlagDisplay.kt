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
        Flag.EXPLOSION -> ItemStack(Material.TNT)
        Flag.FIRE -> ItemStack(Material.FLINT_AND_STEEL)
        Flag.MOB -> ItemStack(Material.CREEPER_HEAD)
        Flag.PISTON -> ItemStack(Material.PISTON)
        Flag.FLUID -> ItemStack(Material.WATER_BUCKET)
        Flag.TREE -> ItemStack(Material.OAK_SAPLING)
        Flag.SCULK -> ItemStack(Material.SCULK_CATALYST)
        Flag.DISPENSER -> ItemStack(Material.DISPENSER)
        Flag.SPONGE -> ItemStack(Material.SPONGE)
        Flag.LIGHTNING -> ItemStack(Material.LIGHTNING_ROD)
        Flag.FALLING_BLOCK -> ItemStack(Material.ANVIL)
        Flag.PASSIVE_ENTITY_VEHICLE -> ItemStack(Material.OAK_BOAT)
    }
}

/**
 * Display names for each flag.
 *
 * @return The set display name for the given flag enum.
 */
fun Flag.getDisplayName(): String {
    return when (this) {
        Flag.EXPLOSION -> getLangText("NameFlagExplosions")
        Flag.FIRE -> getLangText("NameFlagFireSpread")
        Flag.MOB -> getLangText("NameFlagMobGriefing")
        Flag.PISTON -> getLangText("NameFlagPistons")
        Flag.FLUID -> getLangText("NameFlagFluids")
        Flag.TREE -> getLangText("NameFlagTrees")
        Flag.SCULK -> getLangText("NameFlagSculk")
        Flag.DISPENSER -> getLangText("NameFlagDispensers")
        Flag.SPONGE -> getLangText("NameFlagSponge")
        Flag.LIGHTNING -> getLangText("NameFlagLightning")
        Flag.FALLING_BLOCK -> getLangText("NameFlagFallingBlock")
        Flag.PASSIVE_ENTITY_VEHICLE -> getLangText("NameFlagAnimalVehicle")
    }
}

/**
 * Display descriptions for each flag.
 *
 * @return The set display description for the given flag enum.
 */
fun Flag.getDescription(): String {
    return when (this) {
        Flag.EXPLOSION -> getLangText("DescFlagExplosions")
        Flag.FIRE -> getLangText("DescFlagFireSpread")
        Flag.MOB -> getLangText("DescFlagMobGriefing")
        Flag.PISTON -> getLangText("DescFlagPistons")
        Flag.FLUID -> getLangText("DescFlagFluids")
        Flag.TREE -> getLangText("DescFlagTrees")
        Flag.SCULK -> getLangText("DescFlagSculk")
        Flag.DISPENSER -> getLangText("DescFlagDispensers")
        Flag.SPONGE -> getLangText("DescFlagSponge")
        Flag.LIGHTNING -> getLangText("DescFlagLightning")
        Flag.FALLING_BLOCK -> getLangText("DescFlagFallingBlock")
        Flag.PASSIVE_ENTITY_VEHICLE -> getLangText("DescFlagAnimalVehicle")
    }
}