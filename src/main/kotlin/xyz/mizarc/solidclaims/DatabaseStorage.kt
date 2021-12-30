package xyz.mizarc.solidclaims

import org.bukkit.Bukkit
import xyz.mizarc.solidclaims.claims.Claim
import xyz.mizarc.solidclaims.claims.Partition
import xyz.mizarc.solidclaims.claims.PlayerAccess
import xyz.mizarc.solidclaims.events.ClaimPermission
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

/**
 * The gateway to the persistent SQLite based database to hold claim data.
 * @property plugin A reference to the main plugin instance.
 */
class DatabaseStorage(var plugin: SolidClaims) {
    private lateinit var connection: Connection

    /**
     * Creates a connection to the claims database.
     */
    fun openConnection() {
        try {
            connection = DriverManager.getConnection(
                "jdbc:sqlite:" + plugin.dataFolder.toString() + "/claims.db"
            )
            createClaimTable()
            createClaimPartitionTable()
            createClaimPermissionTable()
            createPlayerAccessTable()
            createPlayerStateTable()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Closes the connection to the claims database if opened.
     */
    fun closeConnection() {
        try {
            connection.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Gets a specific claim from the database if available.
     * @param id The unique identifier for the claim.
     * @return The claim object linked to the id. May return null.
     */
    fun getClaim(id: UUID): Claim? {
        try {
            // Get specified claim
            val sqlQuery = "SELECT * FROM claims WHERE id=?;"
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, id.toString())
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                val claimPermissions = getClaimPermissions(id) ?: arrayListOf()
                val playerAccesses: ArrayList<PlayerAccess> =
                    getAllPlayersClaimPermissions(UUID.fromString(resultSet.getString(1))) ?: arrayListOf()

                val claim = Claim(
                    UUID.fromString(resultSet.getString(1)),
                    UUID.fromString(resultSet.getString(2)),
                    Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString(3))),
                    Instant.parse(resultSet.getString(4)),
                    resultSet.getString(5),
                    resultSet.getString(6),
                    claimPermissions,
                    playerAccesses
                )

                val partitions = getClaimPartitionsByClaim(claim)
                if (partitions != null) {
                    claim.partitions = partitions
                }

                return claim
            }
        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }

    fun getClaimsByPlayer(playerId: UUID) : ArrayList<Claim>? {
        try {
            // Get specified claim
            val sqlQuery = "SELECT * FROM claims WHERE ownerId=?;"
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, playerId.toString())
            val resultSet = statement.executeQuery()
            val claims = ArrayList<Claim>()
            while (resultSet.next()) {
                val claimPermissions = getClaimPermissions(playerId) ?: arrayListOf()
                val playerAccesses: ArrayList<PlayerAccess> =
                    getAllPlayersClaimPermissions(UUID.fromString(resultSet.getString(1))) ?: arrayListOf()

                val claim = Claim(
                    UUID.fromString(resultSet.getString(1)),
                    UUID.fromString(resultSet.getString(2)),
                    Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString(3))),
                    Instant.parse(resultSet.getString(4)),
                    resultSet.getString(5),
                    resultSet.getString(6),
                    claimPermissions,
                    playerAccesses
                )

                // Get main partition
                val mainPartition = getMainPartitionByClaim(claim)
                if (mainPartition != null) {
                    claim.mainPartition = mainPartition
                }

                // Get all partitions
                val partitions = getClaimPartitionsByClaim(claim)
                if (partitions != null) {
                    claim.partitions = partitions
                }
                claims.add(claim)
            }

            return claims
        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }

    /**
     * Gets a list of every claim in the database.
     * @return Array of claim objects. May return null.
     */
    fun getAllClaims() : ArrayList<Claim>? {
        val claims: ArrayList<Claim> = arrayListOf()

        try {
            // Get all claims
            val sqlQuery = "SELECT * FROM claims;"
            val statement = connection.prepareStatement(sqlQuery)
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                val claimPermissions: ArrayList<ClaimPermission> =
                    getClaimPermissions(UUID.fromString(resultSet.getString(1))) ?: return null
                val playerAccesses: ArrayList<PlayerAccess> =
                    getAllPlayersClaimPermissions(UUID.fromString(resultSet.getString(1))) ?: return null

                val claim = Claim(
                    UUID.fromString(resultSet.getString(1)),
                    UUID.fromString(resultSet.getString(2)),
                    Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString(3))),
                    Instant.parse(resultSet.getString(4)),
                    resultSet.getString(5),
                    resultSet.getString(6),
                    claimPermissions,
                    playerAccesses
                )

                val mainPartition = getMainPartitionByClaim(claim)
                if (mainPartition != null) {
                    claim.mainPartition = mainPartition
                }

                val partitions = getClaimPartitionsByClaim(claim)
                if (partitions != null) {
                    claim.partitions = partitions
                }

                claims.add(claim)
            }
            return claims

        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }

    /**
     * Adds a new claim instance to the database.
     * @param worldId The unique identifier for the world.
     * @param ownerId The unique identifier for the player.
     */
    fun addClaim(claim: Claim) {
        val sqlQuery = "INSERT INTO claims (id, worldId, ownerId, creationTime) VALUES (?,?,?,?);"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, claim.id.toString())
            statement.setString(2, claim.worldId.toString())
            statement.setString(3, claim.owner.uniqueId.toString())
            statement.setString(4, Instant.now().toString())
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Removes a claim instance from the database.
     * @param id The unique identifier for the claim.
     */
    fun removeClaim(id: UUID) {
        val sqlQuery = "DELETE FROM claims WHERE id=?;"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, id.toString())
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Modifies the name of a claim.
     * @param id The unique identifier for the claim.
     * @param name The name to set the claim to.
     */
    fun modifyClaimName(id: UUID, name: String) {
        val sqlQuery = "UPDATE claims SET name=? WHERE id=?"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, name)
            statement.setString(2, id.toString())
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Modifies the description of a claim.
     * @param id The unique identifier for the claim.
     * @param description The description to set.
     */
    fun modifyClaimDescription(id: UUID, description: String) {
        val sqlQuery = "UPDATE claims SET description=? WHERE id=?"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, description)
            statement.setString(2, id.toString())
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Modifies the description of a claim.
     * @param id The unique identifier for the claim.
     * @param description The description to set.
     */
    fun modifyMainPartition(oldMainPartition: Partition, newMainPartition: Partition) {
        val sqlQuery = "UPDATE claimPartitions SET main=? WHERE firstPositionX=? AND firstPositionZ=? AND " +
                "secondPositionX=? AND secondPositionZ=?;"

        // Set old main to 0
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setInt(1, 0)
            statement.setInt(2, oldMainPartition.area.lowerPosition.x)
            statement.setInt(3, oldMainPartition.area.lowerPosition.z)
            statement.setInt(4, oldMainPartition.area.upperPosition.x)
            statement.setInt(5, oldMainPartition.area.upperPosition.z)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }

        // Set new main to 1
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setInt(1, 1)
            statement.setInt(2, newMainPartition.area.lowerPosition.x)
            statement.setInt(3, newMainPartition.area.lowerPosition.z)
            statement.setInt(4, newMainPartition.area.upperPosition.x)
            statement.setInt(5, newMainPartition.area.upperPosition.z)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Gets a list of all claim partitions associated with a list of claims.
     * @param claims The claims to read from.
     * @return An array of claim partition objects. May return null.
     */
    fun getAllClaimPartitions(claims: ArrayList<Claim>) : ArrayList<Partition>? {
        val partitions: ArrayList<Partition> = arrayListOf()
        for (claim in claims) {
            val claimPartition = getClaimPartitionsByClaim(claim) ?: return null
            partitions.addAll(claimPartition)
        }

        return partitions
    }

    /**
     * Gets a list of claim partitions associated with a claim.
     * @param claim The claim to read from.
     * @return An array of claim partition objects. May return null.
     */
    fun getClaimPartitionsByClaim(claim: Claim) : ArrayList<Partition>? {
        val sqlQuery = "SELECT * FROM claimPartitions WHERE claimId=?;"

        try {
            val claims : ArrayList<Partition> = arrayListOf()
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, claim.id.toString())
            val resultSet = statement.executeQuery()

            while (resultSet.next()) {
                claims.add(Partition(claim,
                    Area(Position(resultSet.getInt(2), resultSet.getInt(3)),
                    Position(resultSet.getInt(4), resultSet.getInt(5)))))
            }

            return claims
        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }

    /**
     * Gets the assigned main partition of a claim.
     * @param claim The claim to read from.
     */
    fun getMainPartitionByClaim(claim: Claim) : Partition? {
        val sqlQuery = "SELECT * FROM claimPartitions WHERE claimId=? AND main=?;"

        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, claim.id.toString())
            statement.setInt(2, 1)
            val resultSet = statement.executeQuery()

            while (resultSet.next()) {
                return (Partition(claim,
                    Area(Position(resultSet.getInt(2), resultSet.getInt(3)),
                        Position(resultSet.getInt(4), resultSet.getInt(5)))))
            }
        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }

    /**
     * Adds a new claim partition to the database.
     * @param id The unique identifier for the claim to be associated with.
     * @param firstLocation The integer pair defining the first location.
     * @param secondLocation The integer pair defining the second location.
     */
    fun addClaimPartition(partition: Partition) {
        val sqlQuery = "INSERT INTO claimPartitions (claimId, firstPositionX, firstPositionZ, " +
                "secondPositionX, secondPositionZ, main) VALUES (?,?,?,?,?,?);"
        try {
            // Set int if partition is the claim's main partition
            var isMain = 0
            if (partition.claim.mainPartition!!.area.lowerPosition == partition.area.lowerPosition)
            {
                isMain = 1
            }

            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, partition.claim.id.toString())
            statement.setInt(2, partition.area.lowerPosition.x)
            statement.setInt(3, partition.area.lowerPosition.z)
            statement.setInt(4, partition.area.upperPosition.x)
            statement.setInt(5, partition.area.upperPosition.z)
            statement.setInt(6, isMain)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Removes a claim partition based on the claim id and partition positions.
     * @param id The unique identifier for the claim to be associated with.
     * @param firstLocation The integer pair defining the first location.
     * @param secondLocation The integer pair defining the second location.
     */
    fun removeClaimPartition(partition: Partition) {
        val sqlQuery = "DELETE FROM claimPartitions WHERE firstPositionX=? AND firstPositionZ=? AND " +
                "secondPositionX=? AND secondPositionZ=?;"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setInt(1, partition.area.lowerPosition.x)
            statement.setInt(2, partition.area.lowerPosition.z)
            statement.setInt(3, partition.area.upperPosition.x)
            statement.setInt(4, partition.area.upperPosition.z)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    fun modifyClaimPartitionLocation(oldPartition: Partition, newPartition: Partition) : Boolean {
        val sqlQuery = "UPDATE claimPartitions SET firstPositionX=?, firstPositionZ=?, " +
                "secondPositionX=?, secondPositionZ=? WHERE firstPositionX=? AND firstPositionZ=? AND " +
                "secondPositionX=? AND secondPositionZ=?;"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setInt(1, newPartition.area.lowerPosition.x)
            statement.setInt(2, newPartition.area.lowerPosition.z)
            statement.setInt(3, newPartition.area.upperPosition.x)
            statement.setInt(4, newPartition.area.upperPosition.z)
            statement.setInt(5, oldPartition.area.lowerPosition.x)
            statement.setInt(6, oldPartition.area.lowerPosition.z)
            statement.setInt(7, oldPartition.area.upperPosition.x)
            statement.setInt(8, oldPartition.area.upperPosition.z)
            statement.executeUpdate()
            statement.close()
            return true
        } catch (error: SQLException) {
            error.printStackTrace()
        }
        return false
    }

    /**
     * Gets the default permissions associated with a claim.
     * @param claimID The unique identifier of the claim.
     * @return An array of ClaimPermissions. May return null.
     */
    fun getClaimPermissions(claimID: UUID) : ArrayList<ClaimPermission>? {
        val sqlQuery = "SELECT * FROM claimPermissions WHERE claimId=?;"

        try {
            val claimPermissions: ArrayList<ClaimPermission> = ArrayList()
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, claimID.toString())
            val permissionResultSet = statement.executeQuery()

            while (permissionResultSet.next()) {
                claimPermissions.add(ClaimPermission.valueOf(permissionResultSet.getString(1)))
            }

            return claimPermissions
        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }

    /**
     * Adds a permission entry to a specific claim from the database.
     * @param claimId The unique identifier for the claim.
     * @param permission The permission enum to assign.
     */
    fun addClaimPermission(claimId: UUID, permission: ClaimPermission) {
        val sqlQuery = "INSERT INTO claimPermissions (claimId, permission) VALUES (?,?);"

        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, claimId.toString())
            statement.setString(2, permission.toString())
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Removes a permission entry for a specific claim from the database.
     * @param claimId The unique identifier for the claim.
     * @param permission The permission enum to remove.
     */
    fun removeClaimPermission(claimId: UUID, permission: ClaimPermission) {
        val sqlQuery = "DELETE FROM claimPermissions WHERE claimId=? AND permission=?;"

        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, claimId.toString())
            statement.setString(2, permission.toString())
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Gets all player permission associated with a particular claim owner.
     * @param ownerID The unique identifier for the claim owner.
     * @return An array of player claim permissions. May return null.
     */
    fun getAllPlayersOwnerPermissions(ownerID: UUID) : ArrayList<PlayerAccess>? {
        val sqlQuery = "SELECT * FROM playerAccess WHERE claimOwnerId=?;"

        try {
            val playerAccesses: ArrayList<PlayerAccess> = arrayListOf()
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, ownerID.toString())
            val resultSet = statement.executeQuery()

            while (resultSet.next()) {
                var foundExistingPlayer = false

                // Add to existing player entry if found
                for (claimPlayer in playerAccesses) {
                    if (UUID.fromString(resultSet.getString(1)) == claimPlayer.id) {
                        claimPlayer.claimPermissions.add(ClaimPermission.valueOf(resultSet.getString(4)))
                        foundExistingPlayer = true
                    }
                }

                // Skip next step if existing player has already been added
                if (foundExistingPlayer) {
                    continue
                }

                // Add new ClaimPlayer entry to list if it doesn't already exist
                val newPlayerAccess = PlayerAccess(UUID.fromString(resultSet.getString(1)))
                newPlayerAccess.claimPermissions.add(ClaimPermission.valueOf(resultSet.getString(4)))
                playerAccesses.add(newPlayerAccess)
            }

            return playerAccesses
        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }

    /**
     * Gets all player permission associated with a particular claim.
     * @param claimID The unique identifier for the claim.
     * @return An array of player claim permissions. May return null.
     */
    fun getAllPlayersClaimPermissions(claimID: UUID) : ArrayList<PlayerAccess>? {
        val sqlQuery = "SELECT * FROM playerAccess WHERE claimId=?;"

        try {
            val playerAccesses: ArrayList<PlayerAccess> = arrayListOf()
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, claimID.toString())
            println(claimID.toString())
            val resultSet = statement.executeQuery()

            while (resultSet.next()) {
                var foundExistingPlayer = false

                // Add to existing player entry if found
                for (playerAccess in playerAccesses) {
                    if (UUID.fromString(resultSet.getString(1)) == playerAccess.id) {
                        playerAccess.claimPermissions.add(ClaimPermission.valueOf(resultSet.getString(4)))
                        foundExistingPlayer = true
                        break
                    }
                }

                // Skip next step if existing player has already been added
                if (foundExistingPlayer) {
                    continue
                }

                // Add new ClaimPlayer entry to list if it doesn't already exist
                val newPlayerAccess = PlayerAccess(UUID.fromString(resultSet.getString(1)))
                newPlayerAccess.claimPermissions.add(ClaimPermission.valueOf(resultSet.getString(4)))
                playerAccesses.add(newPlayerAccess)
            }

            return playerAccesses
        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }

    /**
     * Gets all of a player's permissions for a specified claim owner.
     * @param playerId The unique identifier for the player.
     * @param ownerId The unique identifier for a claim owner.
     * @return A ClaimPlayer object. May return null.
     */
    fun getPlayerOwnerPermissions(playerId: UUID, ownerId: UUID) : PlayerAccess? {
        val sqlQuery = "SELECT * FROM playerAccess WHERE playerId=? AND claimOwnerId=?;"

        try {
            val playerAccess = PlayerAccess(playerId)
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, playerId.toString())
            statement.setString(2, ownerId.toString())
            val resultSet = statement.executeQuery()

            while (resultSet.next()) {
                playerAccess.claimPermissions.add(ClaimPermission.valueOf(resultSet.getString(4)))
            }

            return playerAccess
        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }

    /**
     * Gets all of a player's permissions for a specified claim.
     * @param playerId The unique identifier for the player.
     * @param claimId The unique identifier for the claim.
     * @return A ClaimPlayer object. May Return Null.
     */
    fun getPlayerClaimPermissions(playerId: UUID, claimId: UUID) : PlayerAccess? {
        val sqlQuery = "SELECT * FROM playerAccess WHERE playerId=? AND claimId=?;"

        try {
            val playerAccess = PlayerAccess(playerId)
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, playerId.toString())
            statement.setString(2, claimId.toString())
            val resultSet = statement.executeQuery()

            while (resultSet.next()) {
                playerAccess.claimPermissions.add(ClaimPermission.valueOf(resultSet.getString(4)))
            }

            return playerAccess
        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }

    /**
     * Assigns a player a permission for all of a claim owner's claims.
     * @param playerId The unique identifier for the player to give a permission to.
     * @param claimOwnerId The unique identifier for the claim owner.
     * @param permission The permission key name.
     */
    fun addPlayerOwnerPermission(playerId: UUID, claimOwnerId: UUID, permission: String) {
        val sqlQuery = "INSERT INTO playerAccess (playerId, claimOwnerId, permission) VALUES (?,?,?);"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, playerId.toString())
            statement.setString(2, claimOwnerId.toString())
            statement.setString(3, permission)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Removes a player's permissions from all of a claim owner's claims.
     * @param playerId The unique identifier for the player to give a permission to.
     * @param claimOwnerId The unique identifier for the claim owner.
     * @param permission The permission key name.
     */
    fun removePlayerOwnerPermission(playerId: UUID, claimOwnerId: UUID, permission: String) {
        val sqlQuery = "DELETE FROM playerAccess WHERE playerId=? AND claimOwnerId=? AND permission=?;"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, playerId.toString())
            statement.setString(2, claimOwnerId.toString())
            statement.setString(3, permission)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Assigns a player a permission for a specific claim.
     * @param playerId The unique identifier for the player to give a permission to.
     * @param claimId The unique identifier for the claim.
     * @param permission The permission key name.
     */
    fun addPlayerClaimPermission(playerId: UUID, claimId: UUID, permission: ClaimPermission) {
        val sqlQuery = "INSERT INTO playerAccess (playerId, claimId, permission) VALUES (?,?,?);"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, playerId.toString())
            statement.setString(2, claimId.toString())
            statement.setString(3, permission.toString())
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Removes a player's permissions from a specfic claim.
     * @param playerId The unique identifier for the player to give a permission to.
     * @param claimId The unique identifier for the claim.
     * @param permission The permission key name.
     */
    fun removePlayerClaimPermission(playerId: UUID, claimId: UUID, permission: String) {
        val sqlQuery = "DELETE FROM playerAccess WHERE playerId=? AND claimId=? AND permission=?;"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, playerId.toString())
            statement.setString(2, claimId.toString())
            statement.setString(3, permission)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Gets a player's state from the database.
     *
     */
    fun getPlayerState(playerId: UUID) : PlayerState? {
        val sqlQuery = "SELECT * FROM playerStates WHERE id=?;"

        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, playerId.toString())
            val resultSet = statement.executeQuery()

            while (resultSet.next()) {
                val claims = getClaimsByPlayer(playerId)
                val playerAccess = getAllPlayersOwnerPermissions(playerId)
                return PlayerState(
                    playerId, resultSet.getInt(2), resultSet.getInt(3),
                    resultSet.getInt(4), resultSet.getInt(5), claims!!, playerAccess!!
                )
            }

        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }

    fun getAllPlayerStates() : ArrayList<PlayerState>? {
        val playerStates = ArrayList<PlayerState>()

        try {
            // Get all claims
            val sqlQuery = "SELECT * FROM playerStates;"
            val statement = connection.prepareStatement(sqlQuery)
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                val claims = getClaimsByPlayer(UUID.fromString(resultSet.getString(1)))
                val playerAccess = getAllPlayersOwnerPermissions(UUID.fromString(resultSet.getString(1)))
                playerStates.add(PlayerState(
                    UUID.fromString(resultSet.getString(1)), resultSet.getInt(2), resultSet.getInt(3),
                    resultSet.getInt(4), resultSet.getInt(5), claims!!, playerAccess!!)
                )
            }
            return playerStates

        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }

    /**
     * Adds a new player state instance to the database.
     * @param worldId The unique identifier for the world.
     * @param ownerId The unique identifier for the player.
     */
    fun addPlayerState(playerState: PlayerState) {
        val sqlQuery = "INSERT INTO playerStates (playerId, claimLimit, claimBlockLimit, bonusClaims, bonusClaimBlocks) " +
                "VALUES (?,?,?,?,?);"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, playerState.id.toString())
            statement.setInt(2, playerState.claimLimit)
            statement.setInt(3, playerState.claimBlockLimit)
            statement.setInt(4, playerState.bonusClaims)
            statement.setInt(5, playerState.bonusClaimBlocks)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Removes a player state from the database.
     * @param id The unique identifier for the claim.
     */
    fun removePlayerState(playerState: PlayerState) {
        val sqlQuery = "DELETE FROM playerStates WHERE playerId=?;"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, playerState.id.toString())
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Creates a new table to store claim data if it doesn't exist.
     */
    private fun createClaimTable() {
        val sqlQuery = "CREATE TABLE IF NOT EXISTS claims (id TEXT PRIMARY KEY, worldId TEXT NOT NULL, " +
                "ownerId TEXT NOT NULL, creationTime TEXT NOT NULL, name TEXT, description TEXT);"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Creates a new table to store claim partition data if it doesn't exist.
     */
    private fun createClaimPartitionTable() {
        val sqlQuery = "CREATE TABLE IF NOT EXISTS claimPartitions (claimId TEXT, firstPositionX INTEGER NOT NULL," +
                "firstPositionZ INTEGER NOT NULL, secondPositionX INTEGER NOT NULL, secondPositionZ INTEGER NOT NULL," +
                "main INTEGER NOT NULL);"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Creates a new table to store claim default permission data if it doesn't exist.
     */
    private fun createClaimPermissionTable() {
        val sqlQuery = "CREATE TABLE IF NOT EXISTS claimPermissions (claimId TEXT NOT NULL, " +
                "permission TEXT NOT NULL, FOREIGN KEY (claimId) REFERENCES claims(id));"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Creates a new table to store player permission data if it doesn't exist.
     */
    private fun createPlayerAccessTable() {
        val sqlQuery = "CREATE TABLE IF NOT EXISTS playerAccess (playerId TEXT, claimOwnerId TEXT, " +
                "claimId TEXT, permission TEXT, FOREIGN KEY(claimId) REFERENCES claims(id));"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Creates a new table to store player state data if it doesn't exist.
     */
    private fun createPlayerStateTable() {
        val sqlQuery = "CREATE TABLE IF NOT EXISTS playerStates (playerId TEXT, claimLimit INTEGER, " +
                "claimBlockLimit INTEGER, bonusClaims INTEGER, bonusClaimBlocks INTEGER);"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }
}