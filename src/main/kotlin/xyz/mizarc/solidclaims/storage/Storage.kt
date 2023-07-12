package xyz.mizarc.solidclaims.storage

interface Storage<T> {
    val connection: T
}