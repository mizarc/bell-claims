package xyz.mizarc.solidclaims

interface Mapper<T> {
    fun add(entity: T)
    fun remove(entity: T)
}