package dev.mizarc.bellclaims.domain.values

/**
 * Represents the expected behaviour of certain events in claims that do not pertain to players.
 */
enum class Flag {
    /**
     * When fire can spread from one block to another in the claim.
     */
    FIRE_SPREAD,

    /**
     * When a mob destroys or otherwise changes blocks in the claim.
     */
    MOB_GRIEFING,

    /**
     * When TNT or other entities explode blocks in the claim.
     */
    EXPLOSIONS,

    /**
     * When a piston placed outside of the claim can pull and push blocks in the claim.
     */
    PISTONS,

    /**
     * When fluids can flow into the claim.
     */
    FLUIDS,

    /**
     * When trees planted outside a claim grows into the claim.
     */
    TREES,

    /**
     * When sculk placed outside a claim spreads into the claim.
     */
    SCULK,

    /**
     * When dispensers dispense into the claim.
     */
    DISPENSERS,

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
     * When animals can be placed in vehicles in a claim.
     */
    ANIMAL_VEHICLE
}