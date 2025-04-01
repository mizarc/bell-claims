package dev.mizarc.bellclaims.application.events

import dev.mizarc.bellclaims.domain.partitions.Partition
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Represents an event that occurs when a partition is modified.
 *
 * @property partition The partition that was modified.
 */
class PartitionModificationEvent(val partition: Partition): Event() {
    companion object {
        private val handlerList: HandlerList = HandlerList()

        /**
         * Gets the handlers for this event.
         *
         * @return The handlers for this event.
         */
        @JvmStatic fun getHandlerList(): HandlerList {
            return handlerList
        }
    }

    /**
     * Gets the handlers for this event.
     *
     * @return The handlers for this event.
     */
    override fun getHandlers(): HandlerList {
        return handlerList
    }
}