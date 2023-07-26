package dev.mizarc.bellclaims.storage

interface Storage<T> {
    val connection: T
}