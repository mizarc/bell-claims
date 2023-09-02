package dev.mizarc.bellclaims.api.events

import dev.mizarc.bellclaims.domain.partitions.Partition
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PartitionModificationEvent(val partition: Partition): Event() {
    companion object {
        private val handlerList: HandlerList = HandlerList()
        @JvmStatic fun getHandlerList(): HandlerList {
            return handlerList
        }
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }
}