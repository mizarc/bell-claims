package xyz.mizarc.solidclaims

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ComponentBuilder

class ChatInfoBuilder(var title: String) {
    lateinit var elements: ArrayList<BaseComponent>

    init {
        elements.addAll(ComponentBuilder(title).color(ChatColor.AQUA).create())
    }

    fun addHeader(text: String) {
        elements.addAll(ComponentBuilder(text).color(ChatColor.BLUE).bold(true).create())
    }

    fun addParagraph(text: String) {
        elements.addAll(ComponentBuilder(text).create())
    }

    fun add(left: String, right: String) {
        elements.addAll(ComponentBuilder("${left}: ").color(ChatColor.GOLD).bold(true)
            .append(right).reset().create())
    }

    fun addSpace() {

    }

    fun create() : ArrayList<BaseComponent> {
        return elements
    }
}