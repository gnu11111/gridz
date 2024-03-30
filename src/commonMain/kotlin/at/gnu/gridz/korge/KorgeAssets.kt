package at.gnu.gridz.korge

import korlibs.image.font.Font
import korlibs.image.font.readFont
import korlibs.io.file.std.resourcesVfs

object KorgeAssets {

    enum class Fonts(val filename: String) {
        TITLE("ARCADE.TTF"),
        DIGITAL("Receiptional Receipt.ttf"),
        DEFAULT("joystix monospace.otf");
    }

    private const val FONTS_DIRECTORY = "fonts/"

    private lateinit var fonts: Map<Fonts, Font>


    suspend fun load() {
        fonts = buildMap {
            put(Fonts.DEFAULT, resourcesVfs["$FONTS_DIRECTORY/${Fonts.DEFAULT.filename}"].readFont())
            put(Fonts.TITLE, resourcesVfs["$FONTS_DIRECTORY/${Fonts.TITLE.filename}"].readFont())
            put(Fonts.DIGITAL, resourcesVfs["$FONTS_DIRECTORY/${Fonts.DIGITAL.filename}"].readFont())
        }
    }

    fun font(font: Fonts): Font =
        fonts.getOrElse(font) { fonts[Fonts.DEFAULT]!! }
}
