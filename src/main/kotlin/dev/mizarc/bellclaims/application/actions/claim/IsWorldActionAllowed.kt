package dev.mizarc.bellclaims.application.actions.claim

import dev.mizarc.bellclaims.application.persistence.ClaimFlagRepository
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.results.claim.GetClaimAtPositionResult
import dev.mizarc.bellclaims.application.results.claim.IsWorldActionAllowedResult
import dev.mizarc.bellclaims.domain.values.Flag
import dev.mizarc.bellclaims.domain.values.Position2D
import dev.mizarc.bellclaims.domain.values.WorldActionType
import java.util.UUID

class IsWorldActionAllowed(private val flagRepository: ClaimFlagRepository,
                           private val getClaimAtPosition: GetClaimAtPosition) {
    fun execute(worldId: UUID, position: Position2D, worldActionType: WorldActionType): IsWorldActionAllowedResult {
        // Get the claim at the current position
        val claim = when (val result = getClaimAtPosition.execute(worldId, position)) {
            GetClaimAtPositionResult.NoClaimFound ->  return IsWorldActionAllowedResult.NoClaimFound
            GetClaimAtPositionResult.StorageError -> return IsWorldActionAllowedResult.StorageError
            is GetClaimAtPositionResult.Success -> result.claim
        }

        // Get the flag associated with the action
        val relevantFlag = actionToFlagMapping[worldActionType] ?: return IsWorldActionAllowedResult.NoAssociatedFlag

        // Allow or deny depending on if the claim has the flag or not
        if (flagRepository.doesClaimHaveFlag(claim.id, relevantFlag)) {
            return IsWorldActionAllowedResult.Allowed
        }
        return IsWorldActionAllowedResult.Denied
    }

    private val actionToFlagMapping: Map<WorldActionType, Flag> = mapOf(
        // Fire spread
        WorldActionType.FIRE_BURN to Flag.FIRE,
        WorldActionType.FIRE_SPREAD to Flag.FIRE,

        // Sponge
        WorldActionType.FLUID_ABSORB to Flag.SPONGE,

        // Pistons
        WorldActionType.PISTON_EXTEND to Flag.PISTON,
        WorldActionType.PISTON_RETRACT to Flag.PISTON,

        // Animal Vehicle
        WorldActionType.ANIMAL_ENTER_VEHICLE to Flag.PASSIVE_ENTITY_VEHICLE,

        // Explosions
        WorldActionType.BLOCK_EXPLOSION_DAMAGE_ENTITY to Flag.EXPLOSION,
        WorldActionType.BLOCK_EXPLOSION_DESTROY_BLOCK to Flag.EXPLOSION,
        WorldActionType.ENTITY_EXPLOSION_DAMAGE_ENTITY to Flag.EXPLOSION,
        WorldActionType.ENTITY_EXPLOSION_DESTROY_BLOCK to Flag.EXPLOSION,

        // Dispensers
        WorldActionType.DISPENSE to Flag.DISPENSER,

        // Falling Block
        WorldActionType.FALLING_BLOCK_MATERIALISE to Flag.FALLING_BLOCK,

        // Mob Griefing
        WorldActionType.MOB_DESTROY_BLOCK to Flag.MOB,
        WorldActionType.MOB_DAMAGE_ENTITY to Flag.MOB,

        // Fluids
        WorldActionType.FLUID_FLOW to Flag.FLUID,
        WorldActionType.FLUID_FORM_BLOCK to Flag.FLUID,

        // Trees
        WorldActionType.TREE_GROWTH to Flag.TREE,

        // Sculk
        WorldActionType.SPREAD to Flag.SCULK,

        // Lightning
        WorldActionType.LIGHTNING_DAMAGE to Flag.LIGHTNING,

        // Villager Door
        WorldActionType.VILLAGER_OPEN_DOOR to Flag.VILLAGER_DOOR
    )
}