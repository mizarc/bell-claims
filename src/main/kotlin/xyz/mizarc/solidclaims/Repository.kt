package xyz.mizarc.solidclaims

import java.util.*
import kotlin.collections.ArrayList

interface Repository<T> {
    fun getAll(): ArrayList<T>
    fun getById(id: UUID): T?
    fun add(entity: T)
    fun update(entity: T)
    fun remove(entity: T)
}