package dev.mizarc.bellclaims.domain.values

/**
 * Represents the expected behaviour of certain events in claims and the hierarchy of one permission to any others.
 */
enum class ClaimPermission {
    /**
     * When a block is broken/placed by a player.
     */
    BUILD,

    /**
     * When plants are harvested and replanted by a player.
     */
    HARVEST,

    /**
     * When a container is opened by a player.
     */
    CONTAINER,

    /**
     * When an item is taken or put in display blocks.
     */
    DISPLAY,

    /**
     * When a vehicle is placed or destroyed.
     */
    VEHICLE,

    /**
     * When the sign edit menu is opened.
     */
    SIGN,

    /**
     * When a device used to activate redstone is interacted with by a player.
     */
    REDSTONE,

    /**
     * When a door is opened by a player.
     */
    DOOR,

    /**
     * When a villager or travelling merchant is traded with by a player.
     */
    TRADE,

    /**
     * When a passive mob is interacted with.
     */
    HUSBANDRY,

    /**
     * When an explosive is detonated by a player.
     */
    DETONATION,

    /**
     * When an event is triggered by an omen effect.
     */
    EVENT,

    /**
     * When a player sleeps in a bed or uses a respawn anchor.
     */
    SLEEP
}