package dev.mizarc.bellclaims.listeners

import org.bukkit.event.Event

/**
 * Represents the expected behaviour of certain events in claims that do not pertain to players.
 * NOTE: Unlike [ClaimPermission], this enum does not use a hierarchy to determine which rule takes precedence for any
 * particular event. Due to this, it assumes that every rule works on separate events with no overlap.
 */
enum class ClaimRule(val rules: Array<RuleExecutor>) {
    /**
     * When a block is lit on fire.
     */
    FireSpread(arrayOf(RuleBehaviour.fireSpread, RuleBehaviour.fireBurn)),

    /**
     * When a mob destroys or otherwise changes blocks.
     */
    MobGriefing(arrayOf(RuleBehaviour.mobGriefing)),

    /**
     * When TNT or other entities explode.
     */
    Explosions(arrayOf(RuleBehaviour.entityExplode, RuleBehaviour.blockExplode)),

    /**
     * When a piston extends or retracts and causes other blocks to move.
     */
    Pistons(arrayOf(RuleBehaviour.pistonExtend, RuleBehaviour.pistonRetract));

    companion object {
        /**
         * Get the relevant [RuleExecutor] that handles [event] from [rule].
         */
        fun getRuleExecutorForEvent(event: Class<out Event>, rule: ClaimRule): RuleExecutor? {
            for (re in rule.rules) {
                if (re.eventClass == event) {
                    return re
                }
            }
            return null
        }

        /**
         * Get the [ClaimRule] that handles [event].
         */
        fun getRuleForEvent(event: Class<out Event>): ClaimRule? {
            for (v in values()) {
                for (re in v.rules) {
                    if (re.eventClass == event) {
                        return v
                    }
                }
            }
            return null
        }
    }
}