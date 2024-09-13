package dev.mizarc.bellclaims.domain.permissions

import dev.mizarc.bellclaims.interaction.behaviours.PermissionBehaviour
import dev.mizarc.bellclaims.interaction.behaviours.PermissionExecutor
import org.bukkit.event.Event

/**
 * Represents the expected behaviour of certain events in claims and the hierarchy of one permission to any others.
 */
enum class ClaimPermission(val parent: ClaimPermission?, val events: Array<PermissionExecutor>) {
    /**
     * When a block is broken/placed by a player.
     */
    Build(null, arrayOf(
        PermissionBehaviour.blockBreak,
        PermissionBehaviour.blockPlace,
        PermissionBehaviour.blockMultiPlace,
        PermissionBehaviour.entityPlace,
        PermissionBehaviour.specialEntityDamage,
        PermissionBehaviour.fluidPlace,
        PermissionBehaviour.itemFramePlace,
        PermissionBehaviour.paintingPlace,
        PermissionBehaviour.hangingEntityBreak,
        PermissionBehaviour.fertilize,
        PermissionBehaviour.farmlandStep,
        PermissionBehaviour.dragonEggTeleport,
        PermissionBehaviour.bucketFill,
        PermissionBehaviour.pumpkinShear
    )),

    /**
     * When a container is opened by a player.
     */
    ContainerInspect(null, arrayOf(PermissionBehaviour.openInventory)),

    /**
     * When an item is taken or put in display blocks.
     */
    DisplayManipulate(null, arrayOf(
        PermissionBehaviour.armorStandManipulate,
        PermissionBehaviour.takeLecternBook,
        PermissionBehaviour.flowerPotManipulate,
        PermissionBehaviour.miscDisplayInteractions,
        PermissionBehaviour.miscEntityDisplayInteractions,
        PermissionBehaviour.miscEntityDisplayDamage
    )),

    /**
     * When a vehicle is placed or destroyed.
     */
    VehicleDeploy(null, arrayOf(
        PermissionBehaviour.vehiclePlace,
        PermissionBehaviour.vehicleDestroy
    )),

    /**
     * When the sign edit menu is opened.
     */
    SignEdit(null, arrayOf(
        PermissionBehaviour.signEditing,
        PermissionBehaviour.signDyeing)),

    /**
     * When a device used to activate redstone is interacted with by a player.
     */
    RedstoneInteract(null, arrayOf(PermissionBehaviour.redstoneInteract)),

    /**
     * When a door is opened by a player.
     */
    DoorOpen(null, arrayOf(PermissionBehaviour.openDoor)),

    /**
     * When a villager or travelling merchant is traded with by a player.
     */
    VillagerTrade(null, arrayOf(PermissionBehaviour.villagerTrade)),

    /**
     * When a passive mob is interacted with.
     */
    Husbandry(null, arrayOf(
        PermissionBehaviour.playerDamageEntity,
        PermissionBehaviour.interactAnimals,
        PermissionBehaviour.fishingRod,
        PermissionBehaviour.takeLeadFromFence,
        PermissionBehaviour.beehiveShear,
        PermissionBehaviour.beehiveBottle
    )),

    /**
     * When an explosive is detonated by a player.
     */
    Detonate(null, arrayOf(
        PermissionBehaviour.primeTNT,
        PermissionBehaviour.detonateEndCrystal,
        PermissionBehaviour.detonateBed,
        PermissionBehaviour.detonateRespawnAnchor,
        PermissionBehaviour.detonateTNTMinecart
    )),

    /**
     * When an event is triggered by an omen effect.
     */
    EventStart(null, arrayOf(
        PermissionBehaviour.triggerRaid
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

        /**
         * Get all children of this permission
         */
        fun getAllPermissionChildren(perm: ClaimPermission): Array<ClaimPermission> {
            val children = ArrayList<ClaimPermission>()
            for (v in values()) {
                if (v == perm) continue
                if (v.parent == perm || children.contains(v.parent)) children.add(v)
            }
            return children.toTypedArray()
        }
    }
}