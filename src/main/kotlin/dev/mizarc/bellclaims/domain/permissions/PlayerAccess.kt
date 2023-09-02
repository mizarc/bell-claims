package dev.mizarc.bellclaims.domain.permissions

import dev.mizarc.bellclaims.domain.permissions.ClaimPermission
import java.util.*

/**
 * Maps claims to players to permissions. Each claim can contain multiple players which can each contain multiple
 * permissions.
 * @property id The unique identifier for a player.
 * @property claimPermissions An array of claim permissions.
 */
class PlayerAccess(var claimId: UUID, var playerId: UUID, var claimPermission: ClaimPermission)