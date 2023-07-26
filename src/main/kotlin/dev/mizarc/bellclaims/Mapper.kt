package dev.mizarc.bellclaims

interface Mapper<T> {
    fun add(entity: T)
    fun remove(entity: T)
}