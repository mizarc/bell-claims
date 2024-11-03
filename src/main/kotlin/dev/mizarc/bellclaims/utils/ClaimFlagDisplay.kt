package dev.mizarc.bellclaims.utils

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import dev.mizarc.bellclaims.domain.flags.Flag

import dev.mizarc.bellclaims.utils.getLangText

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
        Flag.Trees -> ItemStack(Material.OAK_SAPLING)
        Flag.Sculk -> ItemStack(Material.SCULK_CATALYST)
        Flag.Dispensers -> ItemStack(Material.DISPENSER)
        Flag.Sponge -> ItemStack(Material.SPONGE)
        Flag.Lightning -> ItemStack(Material.LIGHTNING_ROD)
        Flag.FallingBlock -> ItemStack(Material.ANVIL)
        Flag.EntityVehicle -> ItemStack(Material.OAK_BOAT)
    }
}

/**
 * Display names for each flag.
 *
 * @return The set display name for the given flag enum.
 */
fun Flag.getDisplayName(): String {
    return when (this) {
        Flag.Explosions -> getLangText("NameFlagExplosions")
        Flag.FireSpread -> getLangText("NameFlagFireSpread")
        Flag.MobGriefing -> getLangText("NameFlagMobGriefing")
        Flag.Pistons -> getLangText("NameFlagPistons")
        Flag.Fluids -> getLangText("NameFlagFluids")
        Flag.Trees -> getLangText("NameFlagTrees")
        Flag.Sculk -> getLangText("NameFlagSculk")
        Flag.Dispensers -> getLangText("NameFlagDispensers")
        Flag.Sponge -> getLangText("NameFlagSponge")
        Flag.Lightning -> getLangText("NameFlagLightning")
        Flag.FallingBlock -> getLangText("NameFlagFallingBlock")
        Flag.EntityVehicle -> getLangText("NameFlagEntityVehicle")
    }
}

/**
 * Display descriptions for each flag.
 *
 * @return The set display description for the given flag enum.
 */
fun Flag.getDescription(): String {
    return when (this) {
        Flag.Explosions -> getLangText("DescFlagExplosions")
        Flag.FireSpread -> getLangText("DescFlagFireSpread")
        Flag.MobGriefing -> getLangText("DescFlagMobGriefing")
        Flag.Pistons -> getLangText("DescFlagPistons")
        Flag.Fluids -> getLangText("DescFlagFluids")
        Flag.Trees -> getLangText("DescFlagTrees")
        Flag.Sculk -> getLangText("DescFlagSculk")
        Flag.Dispensers -> getLangText("DescFlagDispensers")
        Flag.Sponge -> getLangText("DescFlagSponge")
        Flag.Lightning -> getLangText("DescFlagLightning")
        Flag.FallingBlock -> getLangText("DescFlagFallingBlock")
        Flag.EntityVehicle -> getLangText("DescFlagEntityVehicle")
    }
}