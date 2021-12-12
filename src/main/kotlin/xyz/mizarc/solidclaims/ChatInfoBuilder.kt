package xyz.mizarc.solidclaims

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ComponentBuilder

class ChatInfoBuilder(var title: String) {
    private var elements: ComponentBuilder

    init {
        var titleLine = ""
        for (i in 0 until 18 - title.length / 2) {
            titleLine += "-"
        }
        titleLine += " $title "
        for (i in 0 until 18 - title.length / 2) {
            titleLine += "-"
        }
        elements = ComponentBuilder(titleLine).color(ChatColor.AQUA)
    }

    fun addHeader(text: String) {
        newLine()
        elements.append(ComponentBuilder(text).color(ChatColor.BLUE).bold(true).create())
    }

    fun addParagraph(text: String) {
        newLine()
        elements.append(ComponentBuilder(text).color(ChatColor.GRAY).create())
    }

    fun addLinked(left: String, right: String) {
        newLine()
        elements.append(ComponentBuilder("${left}: ").color(ChatColor.GOLD)
            .append(right).color(ChatColor.WHITE).create())
    }

    fun addSpace() {
        newLine()
    }

    fun create() : Array<BaseComponent> {
        val finalisedElement = elements.append("\n-------------------------------------").color(ChatColor.AQUA)
        return finalisedElement.create()
    }

    private fun newLine() {
        elements.append("\n")
    }
}