package dev.mizarc.bellclaims.infrastructure.persistence.claims

import dev.mizarc.bellclaims.domain.claims.ClaimRepository
import dev.mizarc.bellclaims.domain.claims.Claim
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import dev.mizarc.bellclaims.domain.partitions.Position3D
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

    private fun preload() {
        try {
            val results = storage.connection.getResults("SELECT * FROM claims")
            for (result in results) {
                val claim = Claim(UUID.fromString(result.getString("id")),
                    UUID.fromString(result.getString("worldId")),
                    Bukkit.getOfflinePlayer(UUID.fromString(result.getString("ownerId"))),
                    Instant.parse(result.getString("creationTime")), result.getString("name"),
                    result.getString("description"), Position3D(result.getInt("positionX"),
                        result.getInt("positionY"), result.getInt("positionZ")),
                    Material.valueOf(result.getString("icon")))
                claims[claim.id] = claim
            }
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    override fun getAll(): Set<Claim> {
        return claims.values.toSet()
    }

    override fun getById(id: UUID): Claim? {
        return claims[id]
    }

    override fun getByPlayer(player: OfflinePlayer): Set<Claim> {
        return claims.values.filter { it.owner.uniqueId == player.uniqueId }.toSet()
    }

    override fun getByPosition(position3D: Position3D, worldId: UUID): Claim? {
        return claims.values.firstOrNull { it.position == position3D && it.worldId == worldId }
    }

    override fun add(claim: Claim) {
        claims[claim.id] = claim
        try {
            storage.connection.executeUpdate("INSERT INTO claims (id, worldId, ownerId, creationTime, name, " +
                    "description, positionX, positionY, positionZ, icon) VALUES (?,?,?,?,?,?,?,?,?,?);",
                claim.id, claim.worldId, claim.owner.uniqueId, claim.creationTime, claim.name, claim.description,
                claim.position.x, claim.position.y, claim.position.z, claim.icon.name)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    override fun update(claim: Claim) {
        claims.remove(claim.id)
        claims[claim.id] = claim
        try {
            storage.connection.executeUpdate("UPDATE claims SET worldId=?, ownerId=?, creationTime=?, name=?, " +
                    "description=?, positionX=?, positionY=?, positionZ=?, icon=? WHERE id=?;",
                claim.worldId, claim.owner.uniqueId, claim.creationTime, claim.name, claim.description,
                claim.position.x, claim.position.y, claim.position.z, claim.icon, claim.id)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    override fun remove(claim: Claim) {
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
                    "description TEXT, positionX INT, positionY INT, positionZ INT, icon TEXT);")
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }
}