package dev.mizarc.bellclaims.infrastructure

import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.util.UUID


class ChatInfoBuilder(private val localizationProvider: LocalizationProvider, private val playerId: UUID,
                      private val title: String) {
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

    fun addRow(text: String) {
        newLine()
        elements.append(Component.text(text, NamedTextColor.WHITE))
    }

    fun addIndexed(index: Int, text: String) {
        newLine()
        val indexedRow = localizationProvider.get(playerId, LocalizationKeys.COMMAND_INFO_BOX_INDEX, index, text)
        elements.append(Component.text(indexedRow, NamedTextColor.WHITE))
    }

    fun addSpace() {
        newLine()
    }

    fun create(): Component {
        val finalisedElement = elements.append(Component.text("\n-----", NamedTextColor.WHITE))
        return finalisedElement.build()
    }

    fun createPaged(currentPage: Int, pages: Int): Component {
        val pageText = localizationProvider.get(
            playerId, LocalizationKeys.COMMAND_INFO_BOX_PAGED, currentPage, pages)
        val finalisedElement = elements.append(Component.text("\n-----", NamedTextColor.WHITE))
            .append(Component.text(pageText, NamedTextColor.DARK_AQUA))
        return finalisedElement.build()
    }

    private fun newLine() {
        elements.append(Component.text("\n"))
    }
}