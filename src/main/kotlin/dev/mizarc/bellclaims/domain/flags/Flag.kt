package dev.mizarc.bellclaims.domain.flags

import dev.mizarc.bellclaims.interaction.behaviours.RuleBehaviour
import dev.mizarc.bellclaims.interaction.behaviours.RuleExecutor
import org.bukkit.event.Event
import org.junit.Rule

/**
 * Represents the expected behaviour of certain events in claims that do not pertain to players.
 * NOTE: Unlike [ClaimPermission], this enum does not use a hierarchy to determine which rule takes precedence for any
 * particular event. Due to this, it assumes that every rule works on separate events with no overlap.
 */
enum class Flag(val rules: Array<RuleExecutor>) {
    /**
     * When a block is lit on fire.
     */
    FireSpread(arrayOf(
        RuleBehaviour.fireSpread,
        RuleBehaviour.fireBurn
    )),

    /**
     * When a mob destroys or otherwise changes blocks.
     */
    MobGriefing(arrayOf(
        RuleBehaviour.mobBlockChange,
        RuleBehaviour.mobBreakDoor,
        RuleBehaviour.mobHangingDamage,
        RuleBehaviour.mobDamageStaticEntity,
        RuleBehaviour.creeperExplode,
        RuleBehaviour.creeperDamageStaticEntity,
        RuleBehaviour.creeperDamageHangingEntity,
        RuleBehaviour.potBreak
    )),

    /**
     * When TNT or other entities explode.
     */
    Explosions(arrayOf(
        RuleBehaviour.entityExplode,
        RuleBehaviour.blockExplode,
        RuleBehaviour.entityExplodeDamage,
        RuleBehaviour.blockExplodeDamage,
        RuleBehaviour.entityExplodeHangingDamage,
        RuleBehaviour.blockExplodeHangingDamage
    )),

    /**
     * When a piston extends or retracts and causes other blocks to move.
     */
    Pistons(arrayOf(
        RuleBehaviour.pistonExtend,
        RuleBehaviour.pistonRetract
    )),

    /**
     * When fluids flow into a claim
     */
    Fluids(arrayOf(
        RuleBehaviour.fluidFlow
    )),

    Trees(arrayOf(
        RuleBehaviour.treeGrowth
    )),

    Sculk(arrayOf(
        RuleBehaviour.sculkSpread
    )),

    Dispensers(arrayOf(
        RuleBehaviour.dispense,
        RuleBehaviour.dispensedSplashPotion,
        RuleBehaviour.dispensedLingeringPotion
    )),

    Sponge(arrayOf(
        RuleBehaviour.spongeAbsorb
    )),

    Lightning(arrayOf(
        RuleBehaviour.lightningDamage
    )),

    FallingBlock(arrayOf(
        RuleBehaviour.blockFall
    ));

    companion object {
        /**
         * Get the relevant [RuleExecutor] that handles [event].
         */
        fun getRuleExecutorForEvent(event: Class<out Event>): RuleExecutor? {
            for (rule in values()) {
                for (ruleExecutor in rule.rules) {
                    if (ruleExecutor.eventClass == event) {
                        return ruleExecutor
                    }
                }
            }
            return null
        }

        /**
         * Get the [Flag] that handles [event].
         */
        fun getRulesForEvent(event: Class<out Event>): Array<Flag> {
            val rules: ArrayList<Flag> = ArrayList()
            for (rule in values()) {
                for (ruleEvent in rule.rules) {
                    if (ruleEvent.eventClass == event) {
                        rules.add(rule)
                    }
                }
            }
            return rules.toTypedArray()
        }
    }
}