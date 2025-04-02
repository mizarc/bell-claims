package dev.mizarc.bellclaims.infrastructure.persistence.claims

import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.infrastructure.persistence.storage.SQLiteStorage
import dev.mizarc.bellclaims.domain.entities.ClaimPermission
import dev.mizarc.bellclaims.application.persistence.ClaimPermissionRepository
import java.sql.SQLException
import java.util.*

class ClaimPermissionRepositorySQLite(private val storage: SQLiteStorage): ClaimPermissionRepository {
    private val permissions: MutableMap<UUID, MutableSet<ClaimPermission>> = mutableMapOf()

    init {
        createTable()
        preload()
    }

    override fun doesClaimHavePermission(claim: Claim, permission: ClaimPermission): Boolean {
        return permissions[claim.id]?.contains(permission) ?: false
    }

    override fun getByClaim(claim: Claim): Set<ClaimPermission> {
        return permissions[claim.id]?.toSet() ?: setOf()
    }

    override fun add(claim: Claim, permission: ClaimPermission) {
        permissions.getOrPut(claim.id) { mutableSetOf() }.add(permission)
        try {
            storage.connection.executeUpdate("INSERT INTO claimPermissions (claimId, permission) " +
                    "VALUES (?,?)", claim.id, permission.name)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    override fun remove(claim: Claim, permission: ClaimPermission) {
        val claimPermissions = permissions[claim.id] ?: return
        claimPermissions.remove(permission)
        if (claimPermissions.isEmpty()) {
            permissions.remove(claim.id)
        }

        try {
            storage.connection.executeUpdate("DELETE FROM claimPermissions WHERE claimId=? AND permission=?",
                claim.id, permission.name)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    override fun removeByClaim(claim: Claim) {
        permissions.remove(claim.id)

        try {
            storage.connection.executeUpdate("DELETE FROM claimPermissions WHERE claimId=?", claim.id)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    fun removeClaim(claim: Claim) {
        permissions.remove(claim.id)

        try {
            storage.connection.executeUpdate("DELETE FROM claimPermissions WHERE claimId=?", claim.id)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Creates a new table to store player permission data if it doesn't exist.
     */
    private fun createTable() {
        try {
            storage.connection.executeUpdate("CREATE TABLE IF NOT EXISTS claimPermissions (claimId TEXT, " +
                    "permission TEXT, FOREIGN KEY(claimId) REFERENCES claims(id));")
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