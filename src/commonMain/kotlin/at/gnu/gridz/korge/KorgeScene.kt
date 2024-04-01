package at.gnu.gridz.korge

import at.gnu.gridz.*
import at.gnu.gridz.GridzGame.Companion.HEIGHT
import at.gnu.gridz.GridzGame.Companion.WIDTH
import korlibs.event.GameButton
import korlibs.event.GameStick
import korlibs.event.Key
import korlibs.event.MouseButton
import korlibs.image.color.Colors
import korlibs.korge.input.gamepad
import korlibs.korge.input.keys
import korlibs.korge.input.singleTouch
import korlibs.korge.scene.AlphaTransition
import korlibs.korge.scene.PixelatedScene
import korlibs.korge.view.*
import korlibs.korge.view.align.alignRightToRightOf
import korlibs.math.geom.Point
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size
import korlibs.math.isEven
import korlibs.time.seconds
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

//class GameScene(private val game: GridzGame, private val scoreWidth: Int)
//    : ScaledScene(WIDTH + scoreWidth, HEIGHT, sceneSmoothing = false) {
class KorgeScene(private val game: GridzGame, private val scoreWidth: Int)
    : PixelatedScene(WIDTH + scoreWidth, HEIGHT, sceneSmoothing = false) {

    private var timerText: Text = Text("")
    private var fpsText: Text = Text("")
    private var frames = 0
    private var lastTime = game.timer
    private var dimmed = SolidRect(0, 0)
    private var transition = false
    private var allowGamepadInput = false

    override suspend fun SContainer.sceneInit() {
        val titleFont = KorgeAssets.font(KorgeAssets.Fonts.TITLE)
        val digitalFont = KorgeAssets.font(KorgeAssets.Fonts.DIGITAL)
        val defaultFont = KorgeAssets.font(KorgeAssets.Fonts.DEFAULT)
        val wall = KorgeAssets.image(KorgeAssets.Images.WALL)
        val teleportColors = listOf(Colors.GREEN, Colors.RED, Colors.BLUE, Colors.ORANGE, Colors.WHITE)
        container {

            container {
                for (row in game.tiles) {
                    for (tile in row) {
                        when (tile) {
                            is Wall -> image(wall) {
                                size(game.tileWidth, game.tileHeight)
                                position((tile.x * game.tileWidth), (tile.y * game.tileHeight))
                            }
                            is Portal -> {
                                circle(
                                    game.tileWidth / 4,
                                    teleportColors[tile.id % teleportColors.size],
                                    Colors.DARKORANGE,
                                    4
                                ) {
                                    position((tile.x + 0.25) * game.tileWidth, (tile.y + 0.25) * game.tileHeight)
                                }
                            }
                            is Exit -> {
                                solidRect(game.tileWidth / 2, game.tileHeight / 2, Colors.RED) {
                                    position((tile.x + 0.25) * game.tileWidth, (tile.y + 0.25) * game.tileHeight)
                                }
                            }
                            else -> {
                                val color = if ((tile.x + tile.y).isEven) Colors["#1d1d1d"] else Colors["#1a1a1a"]
                                tile.component = solidRect(game.tileWidth, game.tileHeight, color) {
                                    position(tile.x * game.tileWidth, tile.y * game.tileHeight)
                                }
                            }
                        }
                    }
                }
                fpsText = text("0", 14, Colors.YELLOW, digitalFont) {
                    position(4, 4)
                }
            }

            container {
                roundRect(Size(scoreWidth - 6, sceneHeight - 6), RectCorners(4), Colors["#301a1a"], Colors["#602020"],
                    6) {
                    position(3, 3)
                }

                text("grid", 48, Colors.ORANGE, titleFont) {
//                setTextBounds(Rectangle(0, 0, scoreWidth, 48))
                    position(30, 10)
                }
                text("Z", 48, Colors.YELLOW, titleFont) {
                    position(108, 10)
                }

                timerText = text("00:00.00", 24, Colors["#d01030"], digitalFont) {
                    position(12, 64)
                }

                text("Level ${game.levelNumber}", 16, Colors.WHITESMOKE, defaultFont) {
                    position(35, 110)
                }
                text(game.level.title, 16, Colors.WHITESMOKE, defaultFont) {
                    position(35, 130)
                }
                text("Best time", 16, Colors.WHITESMOKE, defaultFont) {
                    position(22, 160)
                }
                text("00:00:00", 16, Colors.WHITESMOKE, defaultFont) {
                    position(30, 180)
                }

                for (i in 0..9) {
                    roundRect(Size(24, 24), RectCorners(0), Colors["#402020"], Colors["#a08080"], 2) {
                        position(12 + (i % 5) * 28, 220 + (i / 5) * 28)
                    }
                }

                text("Collect 3 Keys", 12, Colors.WHITESMOKE, defaultFont) {
                    position(10, 290)
                }
                text("Port 4 Times", 12, Colors.WHITESMOKE, defaultFont) {
                    position(10, 310)
                }
                text("Color 50 Tiles", 12, Colors.WHITESMOKE, defaultFont) {
                    position(10, 330)
                }

                alignRightToRightOf(this@sceneInit)
            }

            dimmed = solidRect(WIDTH + scoreWidth, HEIGHT) {
                colorMul = Colors["#00000040"]
                visible = false
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
            down(Key.ENTER) { resetScene() }
            down(Key.P) { pauseScene() }
            down(Key.N) { nextScene() }
            down(Key.B) { previousScene() }
        }

        gamepad {
            button(0) { pressed, button, _ ->
                if (pressed && allowGamepadInput) {
                    when (button) {
                        GameButton.XBOX_B -> resetScene()
                        GameButton.START -> pauseScene()
                        GameButton.L1 -> previousScene()
                        GameButton.R1 -> nextScene()
                        GameButton.SELECT -> views.gameWindow.close(0)
                        else -> {}
                    }
                } else
                    allowGamepadInput = true
            }
        }

        singleTouch {
            tap { pauseScene() }
        }

        addUpdater(referenceFps = 60.fps) { dt ->
            val (dx, dy) = movementInput()
            if (!transition && (game.state == GridzGame.State.LOADED) || (game.state == GridzGame.State.RUNNING)) {
                val events = game.tick(dx, dy)
                timerText.text = game.timer.toDigitalTime()
                player.position(game.x, game.y)
                pointer.position(pointerPostitionFrom(player))
                updateTiles(events)
                updateFps()
            }
        }
    }

    private suspend fun resetScene() {
        transition = true
        game.reset()
        sceneContainer.changeTo(time = 0.5.seconds, transition = AlphaTransition) {
            KorgeScene(game, scoreWidth)
        }
    }

    private fun pauseScene() {
        game.pause()
        dimmed.visible = (game.state == GridzGame.State.PAUSED)
    }

    private suspend fun nextScene() {
        transition = true
        game.next()
        sceneContainer.changeTo(time = 1.0.seconds, transition = AlphaTransition) {
            KorgeScene(game, scoreWidth)
        }
    }

    private suspend fun previousScene() {
        transition = true
        game.previous()
        sceneContainer.changeTo(time = 1.0.seconds, transition = AlphaTransition) {
            KorgeScene(game, scoreWidth)
        }
    }

    private fun updateFps() {
        if (game.state != GridzGame.State.RUNNING) return
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
        val pointerRadius = game.acceleration * player.radius * 0.8
        val pointerX = game.x + (pointerRadius * 0.6 * sin(game.direction))
        val pointerY = game.y - (pointerRadius * 0.6 * cos(game.direction))
        return Point(pointerX, pointerY)
    }

    private fun updateTiles(events: List<GridzEvent>) {
        events.forEach {
            if (it is TileEntered)
                it.tile.component?.color = if ((it.tile.x + it.tile.y).isEven) Colors["#143014"] else Colors["#102810"]
            else if (it is TileLitDeceased)
                it.tile.component?.color = if ((it.tile.x + it.tile.y).isEven) Colors["#1d1d1d"] else Colors["#1a1a1a"]
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
        return if (minutes > 59) "59:59.99" else "${minutes.pad()}:${seconds.pad()}.${hundrets.pad()}"
    }

    private fun Long.pad(): String =
        if (this < 10L) "0$this" else "$this"
}
