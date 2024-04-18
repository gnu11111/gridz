package at.gnu.gridz.korge

import korlibs.audio.sound.Sound
import korlibs.audio.sound.readSound
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
        PLAYER("player.png"),
        WALL("wall.png"),
        KEY("key.png"),
        PILL("pill.png"),
        CLOSED_EXIT("closedExit.png"),
        OPENED_EXIT("openedExit.png"),
        DEFAULT("wall.png");
    }

    enum class Sounds(val filename: String) {
        WALK("walk.wav"),
        CONSUME("consume.wav"),
        COLLECT("collect.wav"),
        TELEPORT("teleport.wav"),
        EXIT_OPENED("exitOpened.wav"),
        GAME_ENDED("gameEnded.wav")
    }


    private const val FONTS_DIRECTORY = "fonts/"
    private const val IMAGES_DIRECTORY = "images/"
    private const val SOUNDS_DIRECTORY = "sounds/"

    private lateinit var fonts: Map<Fonts, Font>
    private lateinit var images: Map<Images, Bitmap>
    private lateinit var sounds: Map<Sounds, Sound>


    suspend fun load() {
        fonts = buildMap {
            Fonts.entries.forEach { put(it, resourcesVfs["$FONTS_DIRECTORY/${it.filename}"].readFont()) }
        }
        images = buildMap {
            Images.entries.forEach { put(it, resourcesVfs["$IMAGES_DIRECTORY/${it.filename}"].readBitmap()) }
        }
        sounds = buildMap {
            Sounds.entries.forEach { put(it, resourcesVfs["$SOUNDS_DIRECTORY/${it.filename}"].readSound()) }
        }
    }

    fun font(font: Fonts): Font =
        fonts.getOrElse(font) { fonts[Fonts.DEFAULT]!! }

    fun image(image: Images): Bitmap =
        images.getOrElse(image) { images[Images.DEFAULT]!! }

    fun sound(sound: Sounds): Sound =
        sounds.getOrElse(sound) { sounds[Sounds.CONSUME]!! }
}
