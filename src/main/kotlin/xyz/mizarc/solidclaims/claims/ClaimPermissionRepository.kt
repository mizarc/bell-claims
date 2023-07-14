package xyz.mizarc.solidclaims.claims

import xyz.mizarc.solidclaims.storage.DatabaseStorage
import xyz.mizarc.solidclaims.listeners.ClaimPermission
import java.sql.SQLException
import java.util.*

class ClaimPermissionRepository(private val storage: DatabaseStorage) {
    private val permissions: MutableMap<UUID, MutableSet<ClaimPermission>> = mutableMapOf()

    init {
        createTable()
        preload()
    }

    fun doesClaimHavePermission(claim: Claim, permission: ClaimPermission): Boolean {
        return permissions[claim.id]?.contains(permission) ?: false
    }

    fun getByClaim(claim: Claim): MutableSet<ClaimPermission> {
        return permissions[claim.id] ?: mutableSetOf()
    }

    fun add(claim: Claim, permission: ClaimPermission) {
        try {
            storage.connection.executeUpdate("INSERT INTO claimPermissions (claimId, permissionId) " +
                    "VALUES (?,?)", claim.id, permission.name)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    fun remove(claim: Claim, permission: ClaimPermission) {
        try {
            storage.connection.executeUpdate("REMOVE FROM claimPermissions WHERE claimId=? AND permissionId=?",
                claim.id, permission.name)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Creates a new table to store player permission data if it doesn't exist.
     */
    private fun createTable() {
        try {
            storage.connection.executeUpdate("CREATE TABLE IF NOT EXISTS claimPermissions (id TEXT, " +
                    "claimId TEXT, permission TEXT, FOREIGN KEY(claimId) REFERENCES claims(id));")
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
            val permission = ClaimPermission.valueOf(result.getString("permission"))
            permissions.getOrPut(UUID.fromString(result.getString("claimId"))) { mutableSetOf() }.add(permission)
        }
    }
}