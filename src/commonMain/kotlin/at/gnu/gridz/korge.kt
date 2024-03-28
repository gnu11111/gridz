package at.gnu.gridz

import at.gnu.gridz.GridzGame.Companion.HEIGHT
import at.gnu.gridz.GridzGame.Companion.WIDTH
import korlibs.event.GameStick
import korlibs.event.Key
import korlibs.event.MouseButton
import korlibs.image.color.Colors
import korlibs.image.font.readFont
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.Korge
import korlibs.korge.input.keys
import korlibs.korge.scene.*
import korlibs.korge.view.*
import korlibs.math.geom.Point
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size
import korlibs.math.isEven
import korlibs.time.seconds
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class GridzRenderer {

    suspend fun init(gridzGame: GridzGame) {
        val scoreWidth = WIDTH / 3
        Korge(
            title = GridzGame.NAME,
            windowSize = Size(WIDTH + scoreWidth, HEIGHT),
            virtualSize = Size(WIDTH + scoreWidth, HEIGHT),
            backgroundColor = Colors[BACKGROUND_COLOR]
        ) {
            sceneContainer().changeTo { GameScene(gridzGame, scoreWidth) }
            addUpdater {
                if (keys[Key.ESCAPE]) views.gameWindow.close(0)
            }
        }
    }

    companion object {
        const val BACKGROUND_COLOR = "#2b1020"
    }
}


//class GameScene(private val game: GridzGame, private val scoreWidth: Int)
//    : ScaledScene(WIDTH + scoreWidth, HEIGHT, sceneSmoothing = false) {
class GameScene(private val game: GridzGame, private val scoreWidth: Int)
    : PixelatedScene(WIDTH + scoreWidth, HEIGHT, sceneSmoothing = false) {

    private var timerText: Text = Text("")
    private var fpsText: Text = Text("")
    private var frames = 0
    private var lastTime = game.timer

    override suspend fun SContainer.sceneInit() {
        graphics {
            for (row in game.tiles) {
                for (tile in row) {
                    val color = if ((tile.x + tile.y).isEven) Colors["#1d1d1d"] else Colors["#1a1a1a"]
                    tile.component = it.solidRect(game.tileWidth, game.tileHeight, color) {
                        position(tile.x * game.tileWidth, tile.y * game.tileHeight)
                    }
                    if (tile.type == GridzTile.TileType.WALL) {
                        it.roundRect(
                            Size(game.tileWidth - 4, game.tileHeight - 4),
                            RectCorners(4),
                            Colors["#0000a0"],
                            Colors["#1020ff"],
                            4
                        ) {
                            position((tile.x * game.tileWidth) + 2, (tile.y * game.tileHeight) + 2)
                        }
                    }
                }
            }
        }
        container {
//            alignLeftToRightOf(game)

            roundRect(Size(scoreWidth - 6, sceneHeight - 6), RectCorners(4), Colors["#202020"], Colors["#a03080"], 6) {
                position(WIDTH + 3, 3)
            }

            val titleFont = resourcesVfs["fonts/ARCADE.TTF"].readFont()
            text("grid", 48, Colors.ORANGE, titleFont) {
//                setTextBounds(Rectangle(0, 0, scoreWidth, 48))
                position(WIDTH + 30, 10)
            }
            text("Z", 48, Colors.RED, titleFont) {
                position(WIDTH + 108, 10)
            }

            val digitalFont = resourcesVfs["fonts/Open 24 Display St.ttf"].readFont()
            timerText = text("00:00:00", 48, Colors.MEDIUMSEAGREEN, digitalFont) {
                position(WIDTH + 10, 48)
            }
            fpsText = text("0", 18, Colors.WHITESMOKE, digitalFont) {
                position(4, 0)
            }

            val infoFont = resourcesVfs["fonts/joystix monospace.otf"].readFont()
            text("Level 1", 16, Colors.WHITESMOKE, infoFont) {
                position(WIDTH + 35, 116)
            }
            text("Best time", 16, Colors.WHITESMOKE, infoFont) {
                position(WIDTH + 22, 145)
            }
            text("00:00:00", 16, Colors.WHITESMOKE, infoFont) {
                position(WIDTH + 30, 165)
            }

            for (i in 0..9) {
                solidRect(24, 24, Colors.DIMGREY) {
                    position(WIDTH + 12 + (i % 5) * 28, 200 + (i / 5) * 28)
                }
            }

            text("Collect 3 Keys", 12, Colors.WHITESMOKE, infoFont) {
                position(WIDTH + 10, 270)
            }
            text("Port 4 Times", 12, Colors.WHITESMOKE, infoFont) {
                position(WIDTH + 10, 290)
            }
            text("Color 50 Tiles", 12, Colors.WHITESMOKE, infoFont) {
                position(WIDTH + 10, 310)
            }
        }
    }

    override suspend fun SContainer.sceneMain() {
        val player = circle(
            radius = game.tileWidth / 2.4,
            fill = Colors.DARKRED,
            stroke = Colors.ORANGE,
            strokeThickness = game.tileWidth * 0.2
        ) {
            anchor(0.4, 0.4)
        }
        val pointer = circle(radius = game.tileWidth / 6, fill = Colors.ANTIQUEWHITE) { anchor(0.5, 0.5) }

        keys {
            down(Key.ENTER) {
                game.resetLevel()
                sceneContainer.changeTo(time = 0.5.seconds, transition = AlphaTransition)  {
                    GameScene(game, scoreWidth)
                }
            }
        }

        addUpdater(referenceFps = 60.fps) { dt ->
            val (dx, dy) = movementInput()
            game.tick(dx, dy, dt)
            timerText.text = game.timer.toDigitalTime()
            player.position(game.x, game.y)
            pointer.position(pointerPostitionFrom(player))
            updateTiles()
            updateFps()
        }
    }

    private fun updateFps() {
        if (!game.started) return
        if ((game.timer - lastTime) < 1000L)
            frames++
        else {
            fpsText.text = frames.toString()
            frames = 0
            lastTime = game.timer
        }
    }

    private fun movementInput(): Pair<Double, Double> {
        val (mouseOrTouchX, mouseOrTouchY) = if (input.mouseButtonPressed(MouseButton.RIGHT))
            mouseOrTouchOffsets(input.mousePos.x, input.mousePos.y)
        else if (input.activeTouches.isNotEmpty())
            mouseOrTouchOffsets(input.touches[0].x, input.touches[0].y)
        else
            0.0 to 0.0
        val stick = input.gamepads[0][GameStick.LEFT]
        val dx = mouseOrTouchX + stick.x + if (keys[Key.LEFT]) -0.5 else if (keys[Key.RIGHT]) 0.5 else 0.0
        val dy = mouseOrTouchY + stick.y + if (keys[Key.DOWN]) -0.5 else if (keys[Key.UP]) 0.5 else 0.0
        return dx.coerceIn(-1.0, 1.0) to dy.coerceIn(-1.0, 1.0)
    }

    private fun pointerPostitionFrom(player: Circle): Point {
        val pointerRadius = game.distance * player.radius * 0.8
        val pointerX = game.x + (pointerRadius * 0.6 * sin(game.angle))
        val pointerY = game.y - (pointerRadius * 0.6 * cos(game.angle))
        return Point(pointerX, pointerY)
    }

    private fun updateTiles() {
        game.tiles.forEach { row ->
            row.forEach {
                if (it.lit > 0)
                    it.component?.color = if ((it.x + it.y).isEven) Colors["#143014"] else Colors["#102810"]
                else
                    it.component?.color = if ((it.x + it.y).isEven) Colors["#1d1d1d"] else Colors["#1a1a1a"]
            }
        }
    }

    private fun mouseOrTouchOffsets(inputX: Double, inputY: Double): Pair<Double, Double> {
        val deltaX = inputX - game.x
        val deltaY = game.y - inputY
        return if ((abs(deltaX) + abs(deltaY)) > 10.0)
            (deltaX / max(abs(deltaX), abs(deltaY))) to (deltaY / max(abs(deltaX), abs(deltaY)))
        else
            0.0 to 0.0
    }

    private fun Long.toDigitalTime(): String {
        val hundrets = (this % 1000L) / 10L
        val seconds = (this % 60000L) / 1000L
        val minutes = (this % 3600000L) / 60000L
        return "${minutes.pad()}:${seconds.pad()}:${hundrets.pad()}"
    }

    private fun Long.pad(): String =
        if (this < 10L) "0$this" else "$this"
}
