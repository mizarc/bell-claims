package xyz.mizarc.solidclaims.claims

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import xyz.mizarc.solidclaims.partitions.Position3D
import xyz.mizarc.solidclaims.storage.DatabaseStorage
import java.sql.SQLException
import java.time.Instant
import java.util.*

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
                    result.getString("description"), Position3D(result.getInt("positionX"),
                        result.getInt("positionY"), result.getInt("positionZ")),
                    Material.valueOf(result.getString("icon")))
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

    fun getByPlayer(player: OfflinePlayer): Set<Claim> {
        return claims.values.filter { it.owner.uniqueId == player.uniqueId }.toSet()
    }

    fun getByPosition(position: Position3D): Claim? {
        for (claim in claims) {
            Bukkit.getLogger().info("${claim.value.position.x}, ${claim.value.position.y} ${claim.value.position.z}")
        }
        Bukkit.getLogger().info("Wah ${position.x}, ${position.y}, ${position.z}")

        return claims.values.firstOrNull { it.position == position }
    }

    fun add(claim: Claim) {
        claims[claim.id] = claim
        try {
            storage.connection.executeUpdate("INSERT INTO claims (id, worldId, ownerId, creationTime, name, " +
                    "description, positionX, positionY, positionZ, icon) VALUES (?,?,?,?,?,?,?,?,?,?);",
                claim.id, claim.worldId, claim.owner.uniqueId, claim.creationTime, claim.position.x, claim.position.y,
                claim.position.z, claim.icon.name)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    fun update(claim: Claim) {
        claims.remove(claim.id)
        claims[claim.id] = claim
        try {
            storage.connection.executeUpdate("UPDATE claims SET worldId=?, ownerId=?, creationTime=?, name=?, " +
                    "description=?, positionX=?, positionY=?, positionZ=? icon=? WHERE id=?;",
                claim.worldId, claim.owner.uniqueId, claim.creationTime, claim.name, claim.description,
                claim.position.x, claim.position.y, claim.position.z, claim.icon, claim.id)
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
                    "description TEXT, positionX TEXT, positionY TEXT, positionZ TEXT, icon TEXT);")
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }
}