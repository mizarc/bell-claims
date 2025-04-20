package dev.mizarc.bellclaims.infrastructure.persistence.claims

import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.values.Position3D
import dev.mizarc.bellclaims.infrastructure.persistence.storage.SQLiteStorage
import java.sql.SQLException
import java.time.Instant
import java.util.*

class ClaimRepositorySQLite(private val storage: SQLiteStorage): ClaimRepository {
    val claims: MutableMap<UUID, Claim> = mutableMapOf()

    init {
        createClaimTable()
        preload()
    }

    override fun getAll(): Set<Claim> {
        return claims.values.toSet()
    }

    override fun getById(id: UUID): Claim? {
        return claims[id]
    }

    override fun getByPlayer(playerId: UUID): Set<Claim> {
        return claims.values.filter { it.playerId == playerId }.toSet()
    }

    override fun getByName(playerId: UUID, name: String): Claim? {
        return claims.values.firstOrNull { it.playerId == playerId && it.name == name }
    }

    override fun getByPosition(position: Position3D, worldId: UUID): Claim? {
        return claims.values.firstOrNull { it.position == position && it.worldId == worldId }
    }

    override fun add(claim: Claim): Boolean {
        claims[claim.id] = claim
        try {
            val rowsAffected = storage.connection.executeUpdate("INSERT INTO claims (id, world_id, owner_id, " +
                    "creation_time, name, description, position_x, position_y, position_z, icon) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?);",
                claim.id, claim.worldId, claim.playerId, claim.creationTime, claim.name, claim.description,
                claim.position.x, claim.position.y, claim.position.z, claim.icon)
            return rowsAffected > 0
        } catch (error: SQLException) {
            throw DatabaseOperationException("Failed to add claim '${claim.name}' to the database. " +
                    "Cause: ${error.message}", error)
        }
    }

    override fun update(claim: Claim): Boolean {
        claims.remove(claim.id)
        claims[claim.id] = claim
        try {
            val rowsAffected = storage.connection.executeUpdate("UPDATE claims SET world_id=?, owner_id=?, " +
                    "creation_time=?, name=?, description=?, position_x=?, " +
                    "position_y=?, position_z=?, icon=? WHERE id=?;",
                claim.worldId, claim.playerId, claim.creationTime, claim.name, claim.description,
                claim.position.x, claim.position.y, claim.position.z, claim.icon, claim.id)
            return rowsAffected > 0
        } catch (error: SQLException) {
            throw DatabaseOperationException("Failed to add update claim '${claim.name}' in the database. " +
                    "Cause: ${error.message}", error)
        }
    }

    override fun remove(claimId: UUID): Boolean {
        claims.remove(claimId)
        try {
            val rowsAffected = storage.connection.executeUpdate("DELETE FROM claims WHERE id=?;", claimId)
            return rowsAffected > 0
        } catch (error: SQLException) {
            throw DatabaseOperationException("Failed to remove claim '$claimId' from the database. " +
                    "Cause: ${error.message}", error)
        }
    }

    /**
     * Creates a new table to store claim data if it doesn't exist.
     */
    private fun createClaimTable() {
        try {
            storage.connection.executeUpdate("CREATE TABLE IF NOT EXISTS claims (id TEXT PRIMARY KEY, " +
                    "world_id TEXT NOT NULL, owner_id TEXT NOT NULL, creation_time TEXT NOT NULL, name TEXT, " +
                    "description TEXT, position_x INT, position_y INT, position_z INT, icon TEXT);")
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Fetches all claims from database and saves it to memory.
     */
    private fun preload() {
        try {
            val results = storage.connection.getResults("SELECT * FROM claims")
            for (result in results) {
                val claim = Claim(UUID.fromString(result.getString("id")),
                    UUID.fromString(result.getString("world_id")),
                    UUID.fromString(result.getString("owner_id")),
                    Instant.parse(result.getString("creation_time")), result.getString("name"),
                    result.getString("description"), Position3D(result.getInt("position_x"),
                        result.getInt("position_y"), result.getInt("position_z")),
                    result.getString("icon"))
                claims[claim.id] = claim
            }
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }
}