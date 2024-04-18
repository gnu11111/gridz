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
            put(Fonts.DEFAULT, resourcesVfs["$FONTS_DIRECTORY/${Fonts.DEFAULT.filename}"].readFont())
            put(Fonts.TITLE, resourcesVfs["$FONTS_DIRECTORY/${Fonts.TITLE.filename}"].readFont())
            put(Fonts.DIGITAL, resourcesVfs["$FONTS_DIRECTORY/${Fonts.DIGITAL.filename}"].readFont())
        }
        images = buildMap {
            put(Images.PLAYER, resourcesVfs["$IMAGES_DIRECTORY/${Images.PLAYER.filename}"].readBitmap())
            put(Images.WALL, resourcesVfs["$IMAGES_DIRECTORY/${Images.WALL.filename}"].readBitmap())
            put(Images.KEY, resourcesVfs["$IMAGES_DIRECTORY/${Images.KEY.filename}"].readBitmap())
            put(Images.PILL, resourcesVfs["$IMAGES_DIRECTORY/${Images.PILL.filename}"].readBitmap())
            put(Images.CLOSED_EXIT, resourcesVfs["$IMAGES_DIRECTORY/${Images.CLOSED_EXIT.filename}"].readBitmap())
            put(Images.OPENED_EXIT, resourcesVfs["$IMAGES_DIRECTORY/${Images.OPENED_EXIT.filename}"].readBitmap())
        }
        sounds = buildMap {
            put(Sounds.WALK, resourcesVfs["$SOUNDS_DIRECTORY/${Sounds.WALK.filename}"].readSound())
            put(Sounds.CONSUME, resourcesVfs["$SOUNDS_DIRECTORY/${Sounds.CONSUME.filename}"].readSound())
            put(Sounds.COLLECT, resourcesVfs["$SOUNDS_DIRECTORY/${Sounds.COLLECT.filename}"].readSound())
            put(Sounds.TELEPORT, resourcesVfs["$SOUNDS_DIRECTORY/${Sounds.TELEPORT.filename}"].readSound())
            put(Sounds.EXIT_OPENED, resourcesVfs["$SOUNDS_DIRECTORY/${Sounds.EXIT_OPENED.filename}"].readSound())
            put(Sounds.GAME_ENDED, resourcesVfs["$SOUNDS_DIRECTORY/${Sounds.GAME_ENDED.filename}"].readSound())
        }
    }

    fun font(font: Fonts): Font =
        fonts.getOrElse(font) { fonts[Fonts.DEFAULT]!! }

    fun image(image: Images): Bitmap =
        images.getOrElse(image) { images[Images.DEFAULT]!! }

    fun sound(sound: Sounds): Sound =
        sounds.getOrElse(sound) { sounds[Sounds.CONSUME]!! }
}
