package dev.mizarc.bellclaims.infrastructure.persistence.claims

import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.PlayerAccessRepository
import dev.mizarc.bellclaims.infrastructure.persistence.storage.SQLiteStorage
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import java.sql.SQLException
import java.util.*

class PlayerAccessRepositorySQLite(private val storage: SQLiteStorage): PlayerAccessRepository {
    private val playerAccess: MutableMap<UUID, MutableMap<UUID, MutableSet<ClaimPermission>>> = mutableMapOf()

    init {
        createTable()
        preload()
    }

    override fun getForAllPlayersInClaim(claimId: UUID): Map<UUID, Set<ClaimPermission>> {
        return playerAccess[claimId]?.toMap() ?: emptyMap()
    }

    override fun getForPlayerInClaim(claimId: UUID, playerId: UUID): Set<ClaimPermission> {
        return playerAccess[claimId]?.get(playerId)?.toSet() ?: emptySet()
    }

    override fun getPlayersWithPermissionInClaim(claimId: UUID): Set<UUID> {
        return playerAccess[claimId]?.keys?.toSet() ?: emptySet()
    }

    override fun add(claimId: UUID, playerId: UUID, permission: ClaimPermission): Boolean {
        playerAccess.getOrPut(claimId) { mutableMapOf() }.getOrPut(playerId) { mutableSetOf() }.add(permission)
        try {
            val rowsAffected = storage.connection.executeUpdate("INSERT INTO playerAccess (claimId, playerId, " +
                    "permission) VALUES (?,?,?)", claimId, playerId, permission.name)
            return rowsAffected > 0
        } catch (error: SQLException) {
            throw DatabaseOperationException("Failed to add permission '${permission}' for claim id '$claimId' to " +
                    "the database. Cause: ${error.message}", error)
        }
    }

    override fun remove(claimId: UUID, playerId: UUID, permission: ClaimPermission): Boolean  {
        val claimPermissions = playerAccess[claimId] ?: return false
        val playerPermissions = claimPermissions[playerId] ?: return false
        playerPermissions.remove(permission)
        if (playerPermissions.isEmpty()) {
            claimPermissions.remove(playerId)
        }

        try {
            val rowsAffected = storage.connection.executeUpdate("DELETE FROM playerAccess WHERE claimId=? AND " +
                    "playerId=? AND permission=?", claimId, playerId, permission.name)
            return rowsAffected > 0
        } catch (error: SQLException) {
            throw DatabaseOperationException("Failed to remove permission '$permission' for player id '$playerId' " +
                    "in claim id '$claimId' from the database. Cause: ${error.message}", error)
        }
    }

    override fun removeByPlayer(claimId: UUID, playerId: UUID): Boolean  {
        val claimPermissions = playerAccess[claimId] ?: return false
        claimPermissions.remove(playerId)

        try {
            val rowsAffected = storage.connection.executeUpdate("DELETE FROM playerAccess WHERE claimId=? " +
                    "AND playerId=?", claimId, playerId)
            return rowsAffected > 0
        } catch (error: SQLException) {
            throw DatabaseOperationException("Failed to remove permissions for player id '$playerId' " +
                    "in claimId '$claimId' from the database. Cause: ${error.message}", error)
        }
    }

    override fun removeByClaim(claimId: UUID): Boolean  {
        playerAccess.remove(claimId)

        try {
            val rowsAffected = storage.connection.executeUpdate("DELETE FROM playerAccess WHERE claimId=?",
                claimId)
            return rowsAffected > 0
        } catch (error: SQLException) {
            throw DatabaseOperationException("Failed to remove permissions for claimId '$claimId' from the database. " +
                    "Cause: ${error.message}", error)
        }
    }

    /**
     * Creates a new table to store player permission data if it doesn't exist.
     */
    private fun createTable() {
        try {
            storage.connection.executeUpdate("CREATE TABLE IF NOT EXISTS playerAccess (claimId TEXT, " +
                    "playerId TEXT, permission TEXT, FOREIGN KEY(claimId) REFERENCES claims(id), UNIQUE (claimId, " +
                    "playerId, permission));")
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