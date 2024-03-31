package at.gnu.gridz.korge

import korlibs.image.bitmap.Bitmap
import korlibs.image.font.Font
import korlibs.image.font.readFont
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs

object KorgeAssets {

    enum class Fonts(val filename: String) {
        TITLE("ARCADE.TTF"),
        DIGITAL("Receiptional Receipt.ttf"),
        DEFAULT("joystix monospace.otf");
    }

    enum class Images(val filename: String) {
        WALL("wall.png"),
        DEFAULT("wall.png");
    }


    private const val FONTS_DIRECTORY = "fonts/"
    private const val IMAGES_DIRECTORY = "images/"

    private lateinit var fonts: Map<Fonts, Font>
    private lateinit var images: Map<Images, Bitmap>


    suspend fun load() {
        fonts = buildMap {
            put(Fonts.DEFAULT, resourcesVfs["$FONTS_DIRECTORY/${Fonts.DEFAULT.filename}"].readFont())
            put(Fonts.TITLE, resourcesVfs["$FONTS_DIRECTORY/${Fonts.TITLE.filename}"].readFont())
            put(Fonts.DIGITAL, resourcesVfs["$FONTS_DIRECTORY/${Fonts.DIGITAL.filename}"].readFont())
        }
        images = buildMap {
            put(Images.WALL, resourcesVfs["$IMAGES_DIRECTORY/${Images.WALL.filename}"].readBitmap())
        }
    }

    fun font(font: Fonts): Font =
        fonts.getOrElse(font) { fonts[Fonts.DEFAULT]!! }

    fun image(image: Images): Bitmap =
        images.getOrElse(image) { images[Images.DEFAULT]!! }
}
