package dev.mizarc.bellclaims

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ComponentBuilder

class ChatInfoBuilder(var title: String) {
    private var elements: ComponentBuilder

    init {
        var titleLine = "§m"
        for (i in 0 until 26 - title.length / 2) {
            titleLine += " "
        }
        titleLine += "§r $title §m"
        for (i in 0 until 26 - title.length / 2) {
            titleLine += " "
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
        var underLine = "§m"
        for (i in 0 until 52) {
            underLine += " "
        }

        val finalisedElement = elements.append("\n${underLine}").color(ChatColor.AQUA)
        return finalisedElement.create()
    }

    fun createPaged(currentPage: Int, pages: Int) : Array<BaseComponent> {
        val pageText = "§r Page ${currentPage}/${pages} §m"
        var underLine = "§m"
        for (i in 0 until 26 - pageText.count() / 2) {
            underLine += " "
        }
        underLine += pageText
        for (i in 0 until 26 - pageText.count() / 2) {
            underLine += " "
        }

        val finalisedElement = elements.append("\n${underLine}").color(ChatColor.AQUA)
        return finalisedElement.create()
    }

    private fun newLine() {
        elements.append("\n").reset()
    }
}