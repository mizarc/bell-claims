package dev.mizarc.bellclaims.api.partitions

interface PartitionEditingService {
    fun addPartition()
    fun resizePartition()
    fun deletePartition()
}