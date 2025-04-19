package dev.mizarc.bellclaims.infrastructure

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor


class ChatInfoBuilder(var title: String) {
    private var elements = Component.text()

    init {
        elements.append(Component.text("-----", NamedTextColor.WHITE))
        elements.append(Component.text(" $title ", NamedTextColor.DARK_AQUA))
        elements.append(Component.text("-----", NamedTextColor.WHITE))
    }

    fun addHeader(text: String) {
        newLine()
        elements.append(Component.text(text, NamedTextColor.BLUE))
    }

    fun addParagraph(text: String) {
        newLine()
        elements.append(Component.text(text, NamedTextColor.GRAY))
    }

    fun addLinked(left: String, right: String) {
        newLine()
        elements.append(Component.text("${left}: ", NamedTextColor.GOLD))
        elements.append(Component.text(right, NamedTextColor.WHITE))
    }

    fun addSpace() {
        newLine()
    }

    fun create(): Component {
        val finalisedElement = elements.append(Component.text("\n-----", NamedTextColor.WHITE))
        return finalisedElement.build()
    }

    fun createPaged(currentPage: Int, pages: Int): Component {
        val pageText = "§r Page ${currentPage}/${pages} §m"
        val finalisedElement = elements.append(Component.text("\n-----", NamedTextColor.WHITE))
            .append(Component.text(pageText, NamedTextColor.DARK_AQUA))
        return finalisedElement.build()
    }

    private fun newLine() {
        elements.append(Component.text("\n"))
    }
}