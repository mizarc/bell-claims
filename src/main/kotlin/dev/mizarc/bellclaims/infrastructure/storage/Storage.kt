package dev.mizarc.bellclaims.infrastructure.storage

interface Storage<T> {
    val connection: T
}