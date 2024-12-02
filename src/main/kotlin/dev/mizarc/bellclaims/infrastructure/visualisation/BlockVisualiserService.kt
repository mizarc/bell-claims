package dev.mizarc.bellclaims.infrastructure.visualisation

import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.partitions.Position3D
import org.bukkit.entity.Player


interface BlockVisualiserService {
    /**
     * Display claim visualisation to target player
     */
    fun show(player: Player): MutableMap<Claim, Set<Position3D>>

    /**
     * Display claim visualisation to target player
     */
    fun show(player: Player, claim: Claim): Set<Position3D>

    /**
     * Hide claim visualisation for target player
     */
    fun hide(player: Player)

    /**
     * Hide claim visualiser for target player after a config specified time
     */
    fun delayedHide(player: Player)

    /**
     * Load a new visualiser display for a target player who is already visualising
     */
    fun refresh(player: Player)
}