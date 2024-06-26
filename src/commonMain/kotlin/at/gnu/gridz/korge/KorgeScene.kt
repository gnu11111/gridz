package at.gnu.gridz.korge

import at.gnu.gridz.*
import at.gnu.gridz.GridzGame.State.*
import at.gnu.gridz.korge.KorgeRenderer.Companion.HEIGHT
import at.gnu.gridz.korge.KorgeRenderer.Companion.INFO_WIDTH
import at.gnu.gridz.korge.KorgeRenderer.Companion.SOUND_ENABLED
import at.gnu.gridz.korge.KorgeRenderer.Companion.WIDTH
import korlibs.audio.sound.Sound
import korlibs.event.*
import korlibs.event.Key
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.input.gamepad
import korlibs.korge.input.keys
import korlibs.korge.input.singleTouch
import korlibs.korge.scene.AlphaTransition
import korlibs.korge.scene.PixelatedScene
import korlibs.korge.service.storage.NativeStorage
import korlibs.korge.service.storage.storage
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.uiButton
import korlibs.korge.view.*
import korlibs.korge.view.align.alignRightToRightOf
import korlibs.math.geom.Point
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size
import korlibs.math.isEven
import korlibs.time.seconds
import kotlin.math.*

class KorgeScene(private val game: GridzGame, storage: NativeStorage)
    : PixelatedScene(WIDTH + INFO_WIDTH, HEIGHT, sceneSmoothing = false) {
////: ScaledScene(WIDTH + INFO_WIDTH, HEIGHT, sceneSmoothing = false) {

    private val tileComponents = mutableMapOf<GridzTile, SolidRect>()
    private val wallComponents = mutableMapOf<GridzTile, Image>()
    private val itemComponents = mutableMapOf<GridzItem, Image>()
    private val inventoryComponents = arrayListOf<RoundRect>()
    private val taskComponents = mutableMapOf<String, Text>()
    private val openedExits = mutableMapOf<GridzTile, Image>()
    private val closedExits = mutableMapOf<GridzTile, Image>()
    private val tileWidth = WIDTH / game.level.cols
    private val tileHeight = HEIGHT / game.level.rows

////private val walkSound = KorgeAssets.sound(KorgeAssets.Sounds.DEFAULT)
    private val consumeSound = KorgeAssets.sound(KorgeAssets.Sounds.CONSUME)
    private val collectSound = KorgeAssets.sound(KorgeAssets.Sounds.COLLECT)
    private val teleportSound = KorgeAssets.sound(KorgeAssets.Sounds.TELEPORT)
    private val exitOpenedSound = KorgeAssets.sound(KorgeAssets.Sounds.EXIT_OPENED)
    private val gameEndedSound = KorgeAssets.sound(KorgeAssets.Sounds.GAME_ENDED)

    private var grid = Container()
    private var info = Container()
    private var timerText: Text = Text("")
    private var bestTime = storage.getOrNull("${game.levelNumber}:bestTime")?.toLongOrNull() ?: -1L
    private var bestTimeText: Text = Text("")
    private var fpsText: Text = Text("")
    private var frames = 0
    private var lastTime = game.timer
    private var dimmed = SolidRect(0, 0)
    private var transition = false
    private var allowGamepadInput = false
    private var pointer = Circle()
    private var from: GridzTile? = null
    private var to: GridzTile? = null
    class MovingWall(val tile: GridzTile, val wall: Image, val dx: Int, val dy: Int)
    private var movingWall: MovingWall? = null
    private var previousButton = UIButton()
    private var pauseButton = UIButton()
    private var resetButton = UIButton()
    private var nextButton = UIButton()

    private lateinit var player: Image


    override suspend fun SContainer.sceneInit() {
        val titleFont = KorgeAssets.font(KorgeAssets.Fonts.TITLE)
////    val digitalFont = KorgeAssets.font(KorgeAssets.Fonts.DIGITAL)
        val defaultFont = KorgeAssets.font(KorgeAssets.Fonts.DEFAULT)
        val playerImage = KorgeAssets.image(KorgeAssets.Images.PLAYER)
        val wallImage = KorgeAssets.image(KorgeAssets.Images.WALL)
        val keyImage = KorgeAssets.image(KorgeAssets.Images.KEY)
        val pillImage = KorgeAssets.image(KorgeAssets.Images.PILL)
        val openedExitImage = KorgeAssets.image(KorgeAssets.Images.OPENED_EXIT)
        val closedExitImage = KorgeAssets.image(KorgeAssets.Images.CLOSED_EXIT)
        val teleportColors = listOf(Colors.GREEN, Colors.RED, Colors.BLUE, Colors.ORANGE, Colors.WHITE)
        val radius = min(tileWidth, tileHeight)

        container {

            grid = container {
                game.tiles.forEach { tile ->
                    val color = tile.backgroundColor()
                    when (tile) {
                        is Wall -> wallComponents[tile] = image(wallImage) {
                            size(tileWidth, tileHeight)
                            position((tile.x * tileWidth), (tile.y * tileHeight))
                        }
                        is Portal -> {
                            tileComponents[tile] = solidRect(tileWidth, tileHeight, color) {
                                position(tile.x * tileWidth, tile.y * tileHeight)
                            }
                            circle(
                                radius / 4,
                                teleportColors[tile.id % teleportColors.size],
                                Colors.DARKORANGE,
                                4
                            ) {
                                position((tile.x + 0.5) * tileWidth, (tile.y + 0.5) * tileHeight)
                                anchor(0.4, 0.4)
                            }
                        }
                        is Exit -> {
                            closedExits[tile] = image(closedExitImage) {
                                size(tileWidth, tileHeight)
                                position(tile.x * tileWidth, tile.y * tileHeight)
                            }
                            openedExits[tile] = image(openedExitImage) {
                                size(tileWidth, tileHeight)
                                position(tile.x * tileWidth, tile.y * tileHeight)
                                visible = false
                            }
                        }
                        else -> {
                            tileComponents[tile] = solidRect(tileWidth, tileHeight, color) {
                                size(tileWidth, tileHeight)
                                position(tile.x * tileWidth, tile.y * tileHeight)
                            }
                        }
                    }
                }
                game.items.forEach { item ->
                    if (item is at.gnu.gridz.Key) {
                        itemComponents[item] = image(keyImage) {
                            size(tileWidth, tileHeight)
                            position((item.x * tileWidth), (item.y * tileHeight))
                        }
                    } else {
                        itemComponents[item] = image(pillImage) {
                            size(tileWidth, tileHeight)
                            position((item.x * tileWidth), (item.y * tileHeight))
                        }
                    }
                }
                fpsText = text("0", 12, Colors.YELLOW, defaultFont) {
                    position(2, 0)
                }
            }

////        player = circle(
////            radius = radius / 2.4,
////            fill = Colors.DARKRED,
////            stroke = Colors.ORANGE,
////            strokeThickness = radius * 0.2
////        ) {
////            anchor(0.4, 0.4)
////        }
            player = image(playerImage) {
                size(tileWidth, tileHeight)
                position((game.x * tileWidth), (game.y * tileHeight))
                anchor(0.5, 0.5)
            }
            pointer = circle(radius = radius / 6.0, fill = Colors.ANTIQUEWHITE) { anchor(0.5, 0.5) }

            info = container {
                roundRect(Size(INFO_WIDTH - 6, sceneHeight - 6), RectCorners(4), Colors["#301a1a"], Colors["#602020"],
                    6) {
                    position(3, 3)
                }

                text("grid", 48, Colors.ORANGE, titleFont) {
////                setTextBounds(Rectangle(0, 0, infoWidth, 48))
                    position(30, 10)
                }
                text("Z", 48, Colors.YELLOW, titleFont) {
                    position(108, 10)
                }

////            timerText = text("00:00.00", 24, Colors["#d01030"], digitalFont) {
                timerText = text("00:00.00", 20, Colors["#d01030"], defaultFont) {
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
                bestTimeText = text(bestTime.toDigitalTime(), 16, Colors.WHITESMOKE, defaultFont) {
                    position(30, 180)
                }

                for (i in 0 until game.level.maxInventory) {
                    inventoryComponents += roundRect(Size(24, 24), RectCorners(0), Colors["#503030"], Colors["#a08080"], 2) {
                        position(12 + (i % 5) * 28, 220 + (i / 5) * 28)
                    }
                }

                game.tasks.entries.forEachIndexed { y, (name, amount) ->
                    taskComponents[name] = text(name.toText(amount), 12, Colors.WHITESMOKE, defaultFont) {
                        position(10, 290 + (y * 20))
                    }
                }

                previousButton = uiButton("<") {
                    size(32, 24)
                    position(10, 444)
                }
                resetButton = uiButton("^") {
                    size(32, 24)
                    position(46, 444)
                }
                pauseButton = uiButton("=") {
                    size(32, 24)
                    position(82, 444)
                }
                nextButton = uiButton(">") {
                    size(32, 24)
                    position(118, 444)
                }

                alignRightToRightOf(this@sceneInit)
            }

            dimmed = solidRect(WIDTH + INFO_WIDTH, HEIGHT) {
                colorMul = Colors["#00000040"]
                visible = false
            }
        }
    }

    override suspend fun SContainer.sceneMain() {

        pauseButton.onPress { pauseScene() }
        nextButton.onPress { dispatchKeyEvent(Key.N) }
        previousButton.onPress { dispatchKeyEvent(Key.B) }
        resetButton.onPress { dispatchKeyEvent(Key.ENTER) }

        keys {
            down(Key.ENTER) { resetScene() }
            down(Key.P) { pauseScene() }
            down(Key.N) { nextScene() }
            down(Key.B) { previousScene() }
            down(Key.SPACE) { if (game.state == GridzGame.State.ENDED) nextScene() }
            down(Key.ESCAPE) { views.gameWindow.close(0) }
        }

        gamepad {
            button(0) { pressed, button, _ ->
                if (pressed && allowGamepadInput) {
                    when (button) {
                        GameButton.XBOX_A -> if (game.state == GridzGame.State.ENDED) nextScene()
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
            tap {
                if ((it.id == 0) && (game.state == GridzGame.State.ENDED))
                    dispatchKeyEvent(Key.N)
            }
        }

        addUpdater(referenceFps = 60.fps) {
////        addFixedUpdater(60.timesPerSecond) {
            val (dx, dy) = movementInput()
            if (!transition) {
                game.tick(dx.toFloat(), dy.toFloat()).handleEvents()
                val x = game.x * tileWidth
                val y = game.y * tileHeight
                player.position(x, y)
                pointer.position(calculatePointerPostition(x, y))
                pointer.color = calculatePointerColor()
                movingWall.updatePosition()
                timerText.text = game.timer.toDigitalTime()
                updateFps()
            }
        }
    }

    private fun List<GridzEvent>.handleEvents() {
        forEach {
            when (it) {
                is TileEntered -> { } //// walkSound.start()
                is TileLit -> {
////                walkSound.start()
                    tileComponents[it.tile]?.color = it.tile.backgroundColor(lit = true)
                    taskComponents[Empty.NAME]?.text = Empty.NAME.toText(game.tasks[Empty.NAME] ?: 0)
                }
                is TileLitDeceased -> tileComponents[it.tile]?.color = it.tile.backgroundColor()
                is WallMoving -> {
                    val from = wallComponents[it.fromTile]
                    val to = tileComponents[it.toTile]
                    if (from != null) {
                        to?.alpha = 0.0
                        val dx = it.toTile.x - it.fromTile.x
                        val dy = it.toTile.y - it.fromTile.y
                        movingWall = MovingWall(it.fromTile, from, dx, dy)
                    }
                }
                is WallMoved -> {
                    movingWall = null
                    val from = wallComponents[it.fromTile]
                    val to = tileComponents[it.toTile]
                    from?.position(it.fromTile.x * tileWidth, it.fromTile.y * tileHeight)
                    to?.position(it.toTile.x * tileWidth, it.toTile.y * tileHeight)
                    to?.color = it.toTile.backgroundColor()
                    to?.alpha = 1.0
                }
                is TeleportStarted -> {
                    teleportSound.start()
                    from = it.from
                    to = it.to
                    tileComponents[from]?.color = Colors["#305050"]
                    tileComponents[to]?.color = Colors["#30a0a0"]
                    player.colorMul = Colors["#ffffff60"]
                }
                is TeleportEnded -> {
                    tileComponents[from]?.color = from?.backgroundColor()!!
                    tileComponents[to]?.color = to?.backgroundColor()!!
                    player.colorMul = Colors["#ffffffff"]
                    taskComponents[Teleport.NAME]?.text = Teleport.NAME.toText(game.tasks[Teleport.NAME] ?: 0)
                }
                is ItemCollected -> {
                    collectSound.start()
                    for (i in game.inventory.indices) {
                        val item = game.inventory[i]
                        val component = itemComponents[item]
                        if (component != null) {
                            if (it.item === item) {
                                grid.removeChild(component)
                                info.addChild(component)
                                component.x = inventoryComponents[i].x
                                component.y = inventoryComponents[i].y
                                component.size = Size(576.0 / tileWidth, 576.0 / tileHeight)
                            } else
                                component.colorMul = Colors["#ffffff80"]
                        }
                    }
                    taskComponents[it.item.name]?.text = it.item.name.toText(game.tasks[it.item.name] ?: 0)
                }
                is ItemConsumed -> {
                    consumeSound.start()
                    itemComponents[it.item]?.visible = false
                    taskComponents[it.item.name]?.text = it.item.name.toText(game.tasks[it.item.name] ?: 0)
                }
                is ExitOpened -> {
                    exitOpenedSound.start()
                    openedExits.values.forEach { exit -> exit.visible = true }
                    closedExits.values.forEach { exit -> exit.visible = false }
                }
                is ActionInProgress -> { }
                is ExitEntered -> {
                    gameEndedSound.start()
                    openedExits[it.exit]?.colorMul = Colors["#ffffff80"]
                    player.colorMul = Colors["#ffffff80"]
                }
                is GameEnded -> {
                    player.colorMul = Colors["#ffffffff"]
                    if ((bestTime < 0L) || (game.timer < bestTime)) {
                        bestTime = game.timer
                        bestTimeText.text = bestTime.toDigitalTime()
                        storage["${game.levelNumber}:bestTime"] = bestTime.toString()
                    }
                }
                is GameReset -> dispatchKeyEvent(Key.ENTER)
            }
        }
    }

    private fun dispatchKeyEvent(key: Key) {
        this@KorgeScene.gameWindow.dispatchDown(KeyEvent(type = KeyEvent.Type.DOWN, key = key))
    }

    private suspend fun resetScene() {
        transition = true
        game.reset()
        sceneContainer.changeTo(time = 0.5.seconds, transition = AlphaTransition) {
            KorgeScene(game, storage)
        }
    }

    private fun pauseScene() {
        game.pause()
        dimmed.visible = (game.state == GridzGame.State.PAUSED)
    }

    private suspend fun nextScene() {
        transition = true
        game.next()
        sceneContainer.changeTo(time = 0.5.seconds, transition = AlphaTransition) {
            KorgeScene(game, storage)
        }
    }

    private suspend fun previousScene() {
        transition = true
        game.previous()
        sceneContainer.changeTo(time = 0.5.seconds, transition = AlphaTransition) {
            KorgeScene(game, storage)
        }
    }

    private fun updateFps() {
        if (game.state != RUNNING) return
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

    private fun calculatePointerPostition(x: Float, y: Float): Point {
        val pointerX = x + (0.25 * game.acceleration * tileWidth * sin(game.direction))
        val pointerY = y - (0.25 * game.acceleration * tileHeight * cos(game.direction))
        return Point(pointerX, pointerY)
    }

    private fun calculatePointerColor(): RGBA =
        if (game.chargeTile.charge <= 0L)
            Colors.ANTIQUEWHITE
        else {
            val level = (128 + 127 * ((game.chargeTile.charge / 16) % 2)).toHex()
            Colors["${level}8080"]
        }

    private fun MovingWall?.updatePosition() {
        if (this == null) return
        val x = if (dx != 0) game.x + dx - 0.5f else tile.x.toFloat()
        val y = if (dy != 0) game.y + dy - 0.5f else tile.y.toFloat()
        wall.position(x * tileWidth, y * tileHeight)
    }

    private fun GridzTile?.backgroundColor(lit: Boolean = false) =
        if ((this == null) || (x + y).isEven)
            if (lit) Colors["#143014"] else Colors["#1d1d1d"]
        else
            if (lit) Colors["#102810"] else Colors["#1a1a1a"]

    private fun Long.toHex() =
        if (this <= 0L)
            "00"
        else if (this >= 255L)
            "ff"
        else
            "0123456789abcdef"[((this / 16) % 16).toInt()].toString() + "0123456789abcdef"[(this % 16).toInt()]

    private fun mouseOrTouchOffsets(inputX: Double, inputY: Double): Pair<Double, Double> {
        val deltaX = inputX - (game.x * tileWidth)
        val deltaY = (game.y * tileHeight) - inputY
        return if ((abs(deltaX) + abs(deltaY)) > 10.0)
            (deltaX / max(abs(deltaX), abs(deltaY))) to (deltaY / max(abs(deltaX), abs(deltaY)))
        else
            0.0 to 0.0
    }

    private fun Long.toDigitalTime(): String {
        if (this < 0L)
            return "--:--:--"
        val minutes = (this % 3600000L) / 60000L
        val seconds = (this % 60000L) / 1000L
        val hundreds = (this % 1000L) / 10L
        return if (minutes > 59) "59:59.99" else "${minutes.pad()}:${seconds.pad()}.${hundreds.pad()}"
    }

    private fun Long.pad(): String =
        if (this < 10L) "0$this" else "$this"

    private fun String.toText(amount: Int): String =
        when (this) {
            at.gnu.gridz.Key.NAME -> "Collect $amount Keys"
            Pill.NAME -> "Eat $amount Pills"
            Teleport.NAME -> "Port $amount Times"
            else -> "Color $amount Tiles"
        }

    private fun Sound.start() {
        if (SOUND_ENABLED) playNoCancel()
    }
}
