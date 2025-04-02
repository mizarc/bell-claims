package dev.mizarc.bellclaims.domain.entities

import dev.mizarc.bellclaims.interaction.behaviours.RuleBehaviour
import dev.mizarc.bellclaims.interaction.behaviours.RuleExecutor
import org.bukkit.event.Event

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
        RuleBehaviour.Companion.fireSpread,
        RuleBehaviour.Companion.fireBurn
    )),

    /**
     * When a mob destroys or otherwise changes blocks.
     */
    MobGriefing(arrayOf(
        RuleBehaviour.Companion.mobBlockChange,
        RuleBehaviour.Companion.mobBreakDoor,
        RuleBehaviour.Companion.mobHangingDamage,
        RuleBehaviour.Companion.mobDamageStaticEntity,
        RuleBehaviour.Companion.creeperExplode,
        RuleBehaviour.Companion.creeperDamageStaticEntity,
        RuleBehaviour.Companion.creeperDamageHangingEntity,
        RuleBehaviour.Companion.potBreak,
        RuleBehaviour.Companion.mobSplashPotion
    )),

    /**
     * When TNT or other entities explode.
     */
    Explosions(arrayOf(
        RuleBehaviour.Companion.entityExplode,
        RuleBehaviour.Companion.blockExplode,
        RuleBehaviour.Companion.entityExplodeDamage,
        RuleBehaviour.Companion.blockExplodeDamage,
        RuleBehaviour.Companion.entityExplodeHangingDamage,
        RuleBehaviour.Companion.blockExplodeHangingDamage
    )),

    /**
     * When a piston extends or retracts and causes other blocks to move.
     */
    Pistons(arrayOf(
        RuleBehaviour.Companion.pistonExtend,
        RuleBehaviour.Companion.pistonRetract
    )),

    /**
     * When fluids flow into a claim
     */
    Fluids(arrayOf(
        RuleBehaviour.Companion.fluidFlow,
        RuleBehaviour.Companion.fluidBlockForm
    )),

    Trees(arrayOf(
        RuleBehaviour.Companion.treeGrowth
    )),

    Sculk(arrayOf(
        RuleBehaviour.Companion.sculkSpread
    )),

    Dispensers(arrayOf(
        RuleBehaviour.Companion.dispense,
        RuleBehaviour.Companion.dispensedSplashPotion,
        RuleBehaviour.Companion.dispensedLingeringPotionSplash,
        RuleBehaviour.Companion.dispensedLingeringPotionEffect
    )),

    Sponge(arrayOf(
        RuleBehaviour.Companion.spongeAbsorb
    )),

    Lightning(arrayOf(
        RuleBehaviour.Companion.lightningDamage
    )),

    FallingBlock(arrayOf(
        RuleBehaviour.Companion.blockFall
    )),

    AnimalVehicle(arrayOf(
        RuleBehaviour.Companion.animalEnterVehicle
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