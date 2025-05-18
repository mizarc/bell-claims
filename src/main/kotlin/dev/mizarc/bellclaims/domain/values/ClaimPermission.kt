package dev.mizarc.bellclaims.domain.values

/**
 * Represents the expected behaviour of certain events in claims and the hierarchy of one permission to any others.
 */
enum class ClaimPermission(private val nameKey: String, private val loreKey: String) {
    /**
     * When a block is broken/placed by a player.
     */
    BUILD(LocalizationKeys.PERMISSION_BUILD_NAME, LocalizationKeys.PERMISSION_BUILD_LORE),

    /**
     * When plants are harvested and replanted by a player.
     */
    HARVEST(LocalizationKeys.PERMISSION_HARVEST_NAME, LocalizationKeys.PERMISSION_HARVEST_LORE),

    /**
     * When a container is opened by a player.
     */
    CONTAINER(LocalizationKeys.PERMISSION_CONTAINER_NAME, LocalizationKeys.PERMISSION_CONTAINER_LORE),

    /**
     * When an item is taken or put in display blocks.
     */
    DISPLAY(LocalizationKeys.PERMISSION_DISPLAY_NAME, LocalizationKeys.PERMISSION_DISPLAY_LORE),

    /**
     * When a vehicle is placed or destroyed.
     */
    VEHICLE(LocalizationKeys.PERMISSION_VEHICLE_NAME, LocalizationKeys.PERMISSION_VEHICLE_LORE),

    /**
     * When the sign edit menu is opened.
     */
    SIGN(LocalizationKeys.PERMISSION_SIGN_NAME, LocalizationKeys.PERMISSION_SIGN_LORE),

    /**
     * When a device used to activate redstone is interacted with by a player.
     */
    REDSTONE(LocalizationKeys.PERMISSION_REDSTONE_NAME, LocalizationKeys.PERMISSION_REDSTONE_LORE),

    /**
     * When a door is opened by a player.
     */
    DOOR(LocalizationKeys.PERMISSION_DOOR_NAME, LocalizationKeys.PERMISSION_DOOR_LORE),

    /**
     * When a villager or travelling merchant is traded with by a player.
     */
    TRADE(LocalizationKeys.PERMISSION_TRADE_NAME, LocalizationKeys.PERMISSION_TRADE_LORE),

    /**
     * When a passive mob is interacted with.
     */
    HUSBANDRY(LocalizationKeys.PERMISSION_HUSBANDRY_NAME, LocalizationKeys.PERMISSION_HUSBANDRY_LORE),

    /**
     * When an explosive is detonated by a player.
     */
    DETONATE(LocalizationKeys.PERMISSION_DETONATE_NAME, LocalizationKeys.PERMISSION_DETONATE_LORE),

    /**
     * When an event is triggered by an omen effect.
     */
    EVENT(LocalizationKeys.PERMISSION_EVENT_NAME, LocalizationKeys.PERMISSION_EVENT_LORE),

    /**
     * When a player sleeps in a bed or uses a respawn anchor.
     */
    SLEEP(LocalizationKeys.PERMISSION_SLEEP_NAME, LocalizationKeys.PERMISSION_SLEEP_LORE)
}