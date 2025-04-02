package dev.mizarc.bellclaims.domain.entities

import dev.mizarc.bellclaims.interaction.behaviours.PermissionBehaviour
import dev.mizarc.bellclaims.interaction.behaviours.PermissionExecutor
import org.bukkit.event.Event

/**
 * Represents the expected behaviour of certain events in claims and the hierarchy of one permission to any others.
 */
enum class ClaimPermission(val events: Array<PermissionExecutor>) {
    /**
     * When a block is broken/placed by a player.
     */
    Build(arrayOf(
        PermissionBehaviour.Companion.blockBreak,
        PermissionBehaviour.Companion.blockPlace,
        PermissionBehaviour.Companion.blockMultiPlace,
        PermissionBehaviour.Companion.entityPlace,
        PermissionBehaviour.Companion.specialEntityDamage,
        PermissionBehaviour.Companion.fluidPlace,
        PermissionBehaviour.Companion.itemFramePlace,
        PermissionBehaviour.Companion.paintingPlace,
        PermissionBehaviour.Companion.hangingEntityBreak,
        PermissionBehaviour.Companion.fertilize,
        PermissionBehaviour.Companion.farmlandStep,
        PermissionBehaviour.Companion.mountFarmlandStep,
        PermissionBehaviour.Companion.dragonEggTeleport,
        PermissionBehaviour.Companion.bucketFill,
        PermissionBehaviour.Companion.pumpkinShear,
        PermissionBehaviour.Companion.potBreak,
        PermissionBehaviour.Companion.armourStandPush
    )),

    /**
     * When plants are harvested and replanted by a player.
     */
    Harvest(arrayOf(
        PermissionBehaviour.Companion.cropHarvest,
        PermissionBehaviour.Companion.cropFertilize
    )),

    /**
     * When a container is opened by a player.
     */
    ContainerInspect(arrayOf(PermissionBehaviour.Companion.openInventory)),

    /**
     * When an item is taken or put in display blocks.
     */
    DisplayManipulate(arrayOf(
        PermissionBehaviour.Companion.armorStandManipulate,
        PermissionBehaviour.Companion.takeLecternBook,
        PermissionBehaviour.Companion.flowerPotManipulate,
        PermissionBehaviour.Companion.miscDisplayInteractions,
        PermissionBehaviour.Companion.miscEntityDisplayInteractions,
        PermissionBehaviour.Companion.miscEntityDisplayDamage
    )),

    /**
     * When a vehicle is placed or destroyed.
     */
    VehicleDeploy(arrayOf(
        PermissionBehaviour.Companion.vehiclePlace,
        PermissionBehaviour.Companion.vehicleDestroy
    )),

    /**
     * When the sign edit menu is opened.
     */
    SignEdit(arrayOf(
        PermissionBehaviour.Companion.signEditing,
        PermissionBehaviour.Companion.signDyeing)),

    /**
     * When a device used to activate redstone is interacted with by a player.
     */
    RedstoneInteract(arrayOf(PermissionBehaviour.Companion.redstoneInteract)),

    /**
     * When a door is opened by a player.
     */
    DoorOpen(arrayOf(PermissionBehaviour.Companion.openDoor)),

    /**
     * When a villager or travelling merchant is traded with by a player.
     */
    VillagerTrade(arrayOf(PermissionBehaviour.Companion.villagerTrade)),

    /**
     * When a passive mob is interacted with.
     */
    Husbandry(arrayOf(
        PermissionBehaviour.Companion.playerDamageEntity,
        PermissionBehaviour.Companion.interactAnimals,
        PermissionBehaviour.Companion.fishingRod,
        PermissionBehaviour.Companion.takeLeadFromFence,
        PermissionBehaviour.Companion.beehiveShear,
        PermissionBehaviour.Companion.beehiveBottle,
        PermissionBehaviour.Companion.tridentLightningDamage,
        PermissionBehaviour.Companion.potionSplash,
        PermissionBehaviour.Companion.potionLinger,
        PermissionBehaviour.Companion.animalPush
    )),

    /**
     * When an explosive is detonated by a player.
     */
    Detonate(arrayOf(
        PermissionBehaviour.Companion.primeTNT,
        PermissionBehaviour.Companion.detonateEndCrystal,
        PermissionBehaviour.Companion.detonateBed,
        PermissionBehaviour.Companion.detonateRespawnAnchor,
        PermissionBehaviour.Companion.detonateTNTMinecart
    )),

    /**
     * When an event is triggered by an omen effect.
     */
    EventStart(arrayOf(
        PermissionBehaviour.Companion.triggerRaid
    )),

    /**
     * When a player sleeps in a bed or uses a respawn anchor.
     */
    Sleep(arrayOf(
        PermissionBehaviour.Companion.bedSleep,
        PermissionBehaviour.Companion.respawnSet
    ));

    companion object {
        /**
         * Get all ClaimPermissions that encompass [event].
         */
        fun getPermissionsForEvent(event: Class<out Event>) : Array<ClaimPermission> {
            val perms: ArrayList<ClaimPermission> = ArrayList()
            for (permission in values()) {
                for (permissionEvent in permission.events) {
                    if (permissionEvent.eventClass == event) {
                        perms.add(permission)
                    }
                }
            }
            return perms.toTypedArray()
        }

        /**
         * Get the first PermissionExecutor that handles [event].
         */
        fun getPermissionExecutorForEvent(event: Class<out Event>): PermissionExecutor? {
            for (v in values()) {
                for (pe in v.events) {
                    if (pe.eventClass == event) {
                        return pe
                    }
                }
            }
            return null
        }
    }
}