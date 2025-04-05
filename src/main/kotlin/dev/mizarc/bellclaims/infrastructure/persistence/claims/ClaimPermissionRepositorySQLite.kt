package dev.mizarc.bellclaims.infrastructure.persistence.claims

import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.infrastructure.persistence.storage.SQLiteStorage
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import dev.mizarc.bellclaims.application.persistence.ClaimPermissionRepository
import java.sql.SQLException
import java.util.*

class ClaimPermissionRepositorySQLite(private val storage: SQLiteStorage): ClaimPermissionRepository {
    private val permissions: MutableMap<UUID, MutableSet<ClaimPermission>> = mutableMapOf()

    init {
        createTable()
        preload()
    }

    override fun doesClaimHavePermission(claimId: UUID, permission: ClaimPermission): Boolean {
        return permissions[claimId]?.contains(permission) == true
    }

    override fun getByClaim(claimId: UUID): Set<ClaimPermission> {
        return permissions[claimId]?.toSet() ?: setOf()
    }

    override fun add(claimId: UUID, permission: ClaimPermission): Boolean {
        // Add to cache
        permissions.getOrPut(claimId) { mutableSetOf() }.add(permission)

        // Add to database
        try {
            val rowsAffected = storage.connection.executeUpdate("INSERT INTO claimPermissions (claimId, permission) " +
                    "VALUES (?,?)", claimId, permission.name)
            return rowsAffected > 0
        } catch (error: SQLException) {
            throw DatabaseOperationException("Failed to add permission '$permission' for claimId '$claimId' to the " +
                    "database. Cause: ${error.message}", error)
        }
    }

    override fun remove(claimId: UUID, permission: ClaimPermission): Boolean {
        // Remove from cache
        val claimPermissions = permissions[claimId] ?: return false
        claimPermissions.remove(permission)
        if (claimPermissions.isEmpty()) permissions.remove(claimId)

        // Remove from database
        try {
            val rowsAffected = storage.connection.executeUpdate("DELETE FROM claimPermissions WHERE claimId=? AND " +
                    "permission=?", claimId, permission.name)
            return rowsAffected > 0
        } catch (error: SQLException) {
            throw DatabaseOperationException("Failed to remove permission '$permission' for claimId '$claimId' from " +
                    "the database. Cause: ${error.message}", error)
        }
    }

    override fun removeByClaim(claimId: UUID): Boolean {
        // Remove from cache
        permissions.remove(claimId)

        // Remove from database
        try {
            val rowsAffected = storage.connection.executeUpdate("DELETE FROM claimPermissions WHERE claimId=?", claimId)
            return rowsAffected > 0
        } catch (error: SQLException) {
            throw DatabaseOperationException("Failed to remove all permissions for claim $claimId from the database. " +
                    "Cause: ${error.message}", error)
        }
    }

    /**
     * Creates a new table to store player permission data if it doesn't exist.
     */
    private fun createTable() {
        try {
            storage.connection.executeUpdate("CREATE TABLE IF NOT EXISTS claimPermissions (claimId TEXT, " +
                    "permission TEXT, FOREIGN KEY(claimId) REFERENCES claims(id), UNIQUE (claimId, permission))")
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Fetches all player access permissions from database and saves it to memory.
     */
    private fun preload() {
        val results = storage.connection.getResults("SELECT * FROM claimPermissions")
        for (result in results) {
            try {
                val permission = ClaimPermission.valueOf(result.getString("permission"))
                permissions.getOrPut(UUID.fromString(result.getString("claimId"))) { mutableSetOf() }.add(permission)
            }
            catch (error: IllegalArgumentException) {
                continue
            }
        }
    }
}