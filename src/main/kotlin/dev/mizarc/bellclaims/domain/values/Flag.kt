package dev.mizarc.bellclaims.domain.values

/**
 * Represents the expected behaviour of certain events in claims that do not pertain to players.
 */
enum class Flag {
    /**
     * When fire can spread from one block to another in the claim.
     */
    FIRE,

    /**
     * When a mob destroys or otherwise changes blocks in the claim.
     */
    MOB,

    /**
     * When TNT or other entities explode blocks in the claim.
     */
    EXPLOSION,

    /**
     * When a piston placed outside of the claim can pull and push blocks in the claim.
     */
    PISTON,

    /**
     * When fluids can flow into the claim.
     */
    FLUID,

    /**
     * When trees planted outside a claim grows into the claim.
     */
    TREE,

    /**
     * When sculk placed outside a claim spreads into the claim.
     */
    SCULK,

    /**
     * When dispensers dispense into the claim.
     */
    DISPENSER,

    /**
     * When sponge placed outside the claim can drain water in the claim.
     */
    SPONGE,

    /**
     * When lighting can cause damage to a claim.
     */
    LIGHTNING,

    /**
     * When falling blocks can materialise when landing in a claim.
     */
    FALLING_BLOCK,

    /**
     * When passive entities can be placed in vehicles in a claim.
     */
    PASSIVE_ENTITY_VEHICLE
}