package dev.mizarc.bellclaims.claims

import dev.mizarc.bellclaims.listeners.ClaimPermission
import java.util.*

/**
 * Maps claims to players to permissions. Each claim can contain multiple players which can each contain multiple
 * permissions.
 * @property id The unique identifier for a player.
 * @property claimPermissions An array of claim permissions.
 */
class PlayerAccess(var claimId: UUID, var playerId: UUID, var claimPermission: ClaimPermission)