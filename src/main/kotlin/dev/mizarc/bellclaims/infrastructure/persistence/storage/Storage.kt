package dev.mizarc.bellclaims.infrastructure.persistence.storage

interface Storage<T> {
    val connection: T
}