package dev.mizarc.bellclaims.domain.values

/**
 * Represents the expected behaviour of certain events in claims that do not pertain to players.
 */
enum class Flag(private val nameKey: String, private val loreKey: String) {
    /**
     * When fire can spread from one block to another in the claim.
     */
    FIRE(LocalizationKeys.FLAG_FIRE_NAME,
        LocalizationKeys.FLAG_FIRE_LORE),

    /**
     * When a mob destroys or otherwise changes blocks in the claim.
     */
    MOB(LocalizationKeys.FLAG_MOB_NAME,
        LocalizationKeys.FLAG_MOB_LORE),

    /**
     * When TNT or other entities explode blocks in the claim.
     */
    EXPLOSION(LocalizationKeys.FLAG_EXPLOSION_NAME,
        LocalizationKeys.FLAG_EXPLOSION_LORE),

    /**
     * When a piston placed outside of the claim can move blocks in the claim.
     */
    PISTON(LocalizationKeys.FLAG_PISTON_NAME,
        LocalizationKeys.FLAG_PISTON_LORE),

    /**
     * When fluids can flow into the claim.
     */
    FLUID(LocalizationKeys.FLAG_FLUID_NAME,
        LocalizationKeys.FLAG_FLUID_LORE),

    /**
     * When trees planted outside a claim grows into the claim.
     */
    TREE(LocalizationKeys.FLAG_TREE_NAME,
        LocalizationKeys.FLAG_TREE_LORE),

    /**
     * When sculk placed outside a claim spreads into the claim.
     */
    SCULK(LocalizationKeys.FLAG_SCULK_NAME,
        LocalizationKeys.FLAG_SCULK_LORE),

    /**
     * When dispensers dispense into the claim.
     */
    DISPENSER(LocalizationKeys.FLAG_DISPENSER_NAME,
        LocalizationKeys.FLAG_DISPENSER_LORE),

    /**
     * When sponge placed outside the claim can drain water in the claim.
     */
    SPONGE(LocalizationKeys.FLAG_SPONGE_NAME,
        LocalizationKeys.FLAG_SPONGE_LORE),

    /**
     * When lighting can cause damage to a claim.
     */
    LIGHTNING(LocalizationKeys.FLAG_LIGHTNING_NAME,
        LocalizationKeys.FLAG_LIGHTNING_LORE),

    /**
     * When falling blocks can materialise when landing in a claim.
     */
    FALLING_BLOCK(LocalizationKeys.FLAG_FALLING_BLOCK_NAME,
        LocalizationKeys.FLAG_FALLING_BLOCK_LORE),

    /**
     * When passive entities can be placed in vehicles in a claim.
     */
    PASSIVE_ENTITY_VEHICLE(LocalizationKeys.FLAG_PASSIVE_ENTITY_VEHICLE_NAME,
        LocalizationKeys.FLAG_PASSIVE_ENTITY_VEHICLE_LORE)
}