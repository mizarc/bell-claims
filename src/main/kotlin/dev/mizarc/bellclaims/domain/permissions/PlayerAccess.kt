package dev.mizarc.bellclaims.domain.permissions

import java.util.*

/**
 * Maps claims to players to permissions. Each claim can contain multiple players which can each contain multiple
 * permissions.
 * @property claimId The unique identifier for the claim.
 * @property playerId The unique identifier for a player.
 * @property claimPermission An array of claim permissions.
 */
class PlayerAccess(var claimId: UUID, var playerId: UUID, var claimPermission: ClaimPermission)