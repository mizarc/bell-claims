package dev.mizarc.bellclaims.infrastructure.persistence.claims

import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.application.persistence.PlayerAccessRepository
import org.bukkit.OfflinePlayer
import dev.mizarc.bellclaims.infrastructure.persistence.storage.SQLiteStorage
import dev.mizarc.bellclaims.domain.permissions.ClaimPermission
import java.sql.SQLException
import java.util.*

class PlayerAccessRepositorySQLite(private val storage: SQLiteStorage): PlayerAccessRepository {
    private val playerAccess: MutableMap<UUID, MutableMap<UUID, MutableSet<ClaimPermission>>> = mutableMapOf()

    init {
        createTable()
        preload()
    }

    override fun getByClaim(claim: Claim): Map<UUID, Set<ClaimPermission>> {
        return playerAccess[claim.id]?.toMap() ?: emptyMap()
    }

    override fun getByPlayer(claim: Claim, player: OfflinePlayer): Set<ClaimPermission> {
        return playerAccess[claim.id]?.get(player.uniqueId)?.toSet() ?: emptySet()
    }

    override fun add(claim: Claim, player: OfflinePlayer, permission: ClaimPermission) {
        playerAccess.getOrPut(claim.id) { mutableMapOf() }.getOrPut(player.uniqueId) { mutableSetOf() }.add(permission)
        try {
            storage.connection.executeUpdate("INSERT INTO playerAccess (claimId, playerId, permission) " +
                    "VALUES (?,?,?)", claim.id, player.uniqueId, permission.name)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    override fun remove(claim: Claim, player: OfflinePlayer, permission: ClaimPermission) {
        val claimPermissions = playerAccess[claim.id] ?: return
        val playerPermissions = claimPermissions[player.uniqueId] ?: return
        playerPermissions.remove(permission)
        if (playerPermissions.isEmpty()) {
            claimPermissions.remove(player.uniqueId)
        }

        try {
            storage.connection.executeUpdate("DELETE FROM playerAccess WHERE claimId=? AND playerId=? " +
                    "AND permission=?", claim.id, player.uniqueId, permission.name)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    override fun removeByPlayer(claim: Claim, player: OfflinePlayer) {
        val claimPermissions = playerAccess[claim.id] ?: return
        claimPermissions.remove(player.uniqueId)

        try {
            storage.connection.executeUpdate("DELETE FROM playerAccess WHERE claimId=? AND playerId=?",
                claim.id, player.uniqueId)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    override fun removeByClaim(claim: Claim) {
        playerAccess.remove(claim.id)

        try {
            storage.connection.executeUpdate("DELETE FROM playerAccess WHERE claimId=?", claim.id)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Creates a new table to store player permission data if it doesn't exist.
     */
    private fun createTable() {
        try {
            storage.connection.executeUpdate("CREATE TABLE IF NOT EXISTS playerAccess (claimId TEXT, " +
                    "playerId TEXT, permission TEXT, FOREIGN KEY(claimId) REFERENCES claims(id));")
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Fetches all player access permissions from database and saves it to memory.
     */
    private fun preload() {
        val results = storage.connection.getResults("SELECT * FROM playerAccess")
        for (result in results) {
            val playerId = UUID.fromString(result.getString("playerId"))
            val claimId = UUID.fromString(result.getString("claimId"))
            try {
                val permission = ClaimPermission.valueOf(result.getString("permission"))
                val claimPlayers = playerAccess
                    .getOrPut(claimId) { mutableMapOf(playerId to mutableSetOf()) }
                claimPlayers.getOrPut(playerId) { mutableSetOf() }.add(permission)
            }
            catch (error: IllegalArgumentException) {
                continue
            }
        }
    }
}