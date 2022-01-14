package xyz.mizarc.solidclaims.storage

interface Storage<T> {
    fun getConnection(): T
}