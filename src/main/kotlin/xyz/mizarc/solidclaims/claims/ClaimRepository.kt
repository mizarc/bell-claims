package xyz.mizarc.solidclaims.claims

import org.bukkit.Bukkit
import xyz.mizarc.solidclaims.partitions.Position
import xyz.mizarc.solidclaims.storage.DatabaseStorage
import java.sql.SQLException
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

class ClaimRepository(private val storage: DatabaseStorage) {
    val claims: MutableMap<UUID, Claim> = mutableMapOf()

    init {
        createClaimTable()
        preload()
    }

    private fun preload() {
        try {
            val results = storage.connection.getResults("SELECT * FROM claims")
            for (result in results) {
                val claim = Claim(UUID.fromString(result.getString("id")),
                    UUID.fromString(result.getString("worldId")),
                    Bukkit.getOfflinePlayer(UUID.fromString(result.getString("ownerId"))),
                    Instant.parse(result.getString("creationTime")), result.getString("name"),
                    result.getString("description"), Position(result.getInt("positionX"),
                    result.getInt("positionZ")))
                claims[claim.id] = claim
            }
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    fun getAll(): Set<Claim> {
        return claims.values.toSet()
    }

    fun getById(id: UUID): Claim? {
        return claims[id]
    }

    fun getByPlayer(playerId: UUID): Set<Claim> {
        val foundClaims: MutableSet<Claim> = mutableSetOf()
        for (claim in claims) {
            if (claim.value.owner == Bukkit.getOfflinePlayer(playerId)) {
                foundClaims.add(claim.value)
            }
        }
        return foundClaims
    }

    fun getByPosition(position: Position): Claim? {
        return claims.values.firstOrNull { it.position == position }
    }

    fun add(claim: Claim) {
        claims[claim.id] = claim
        try {
            storage.connection.executeUpdate("INSERT INTO claims (id, worldId, ownerId, creationTime, " +
                    "positionX, positionZ) VALUES (?,?,?,?,?,?);", claim.id, claim.worldId, claim.owner,
                claim.creationTime, claim.position.x, claim.position.z)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    fun update(claim: Claim) {
        claims.remove(claim.id)
        claims[claim.id] = claim
        try {
            storage.connection.executeUpdate("UPDATE claims SET worldId=?, ownerId=?, creationTime=?, " +
                    "positionX=?, positionZ=? WHERE id=?;", claim.worldId, claim.owner,
                claim.creationTime, claim.position.x, claim.position.z, claim.id)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    fun remove(claim: Claim) {
        claims.remove(claim.id)
        try {
            storage.connection.executeUpdate("DELETE FROM claims WHERE id=?;", claim.id)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Creates a new table to store claim data if it doesn't exist.
     */
    private fun createClaimTable() {
        try {
            storage.connection.executeUpdate("CREATE TABLE IF NOT EXISTS claims (id TEXT PRIMARY KEY, " +
                    "worldId TEXT NOT NULL, ownerId TEXT NOT NULL, creationTime TEXT NOT NULL, name TEXT, " +
                    "description TEXT, positionX TEXT, positionZ, NOT NULL);")
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }
}