package at.gnu.gridz

import at.gnu.gridz.GridzGame.Companion.HEIGHT
import at.gnu.gridz.GridzGame.Companion.WIDTH
import korlibs.event.GameStick
import korlibs.event.Key
import korlibs.event.MouseButton
import korlibs.image.color.Colors
import korlibs.korge.Korge
import korlibs.korge.scene.PixelatedScene
import korlibs.korge.scene.sceneContainer
import korlibs.korge.view.*
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size
import korlibs.math.isEven
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
        }
    }

    companion object {
        const val BACKGROUND_COLOR = "#2b0000"
    }
}


//class GameScene(private val game: GridzGame, private val scoreWidth: Int) : ScaledScene(WIDTH + scoreWidth, HEIGHT) {
class GameScene(private val game: GridzGame, private val scoreWidth: Int)
    : PixelatedScene(WIDTH + scoreWidth, HEIGHT) {

    override suspend fun SContainer.sceneInit() {
        for (row in game.tiles) {
            for (tile in row) {
                val color = if ((tile.x + tile.y).isEven) Colors["#1d1d1d"] else Colors["#1a1a1a"]
                tile.component = solidRect(game.tileWidth, game.tileHeight, color) {
                    position(tile.x * game.tileWidth, tile.y * game.tileHeight)
                }
                if (tile.type == GridzTile.TileType.WALL) {
                    roundRect(
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
        solidRect(scoreWidth, HEIGHT, Colors["#201515"]) { position(WIDTH, 0) }

        addUpdater(referenceFps = 60.fps) { dt ->
            val (dx, dy) = getInput()
            game.tick(dx, dy, dt)
            val pointerRadius = game.distance * player.radius * 0.8
            val pointerX = game.x + (pointerRadius * 0.6 * sin(game.angle))
            val pointerY = game.y - (pointerRadius * 0.6 * cos(game.angle))
            player.position(game.x, game.y)
            pointer.position(pointerX, pointerY)
            game.tiles.forEach { row ->
                row.forEach {
                    if (it.lit > 0)
                        it.component?.color = if ((it.x + it.y).isEven) Colors["#142414"] else Colors["#102810"]
                    else
                        it.component?.color = if ((it.x + it.y).isEven) Colors["#1d1d1d"] else Colors["#1a1a1a"]
                }
            }
        }
    }

    private fun getInput(): Pair<Double, Double> {
        val mouseX: Double
        val mouseY: Double
        if (input.mouseButtonPressed(MouseButton.RIGHT)) {
            val deltaX = input.mousePos.x - game.x
            val deltaY = game.y - input.mousePos.y
            if ((abs(deltaX) + abs(deltaY)) > 5.0) {
                mouseX = deltaX / max(abs(deltaX), abs(deltaY))
                mouseY = deltaY / max(abs(deltaX), abs(deltaY))
            } else {
                mouseX = 0.0
                mouseY = 0.0
            }
        } else {
            mouseX = 0.0
            mouseY = 0.0
        }
        val stick = input.gamepads[0][GameStick.LEFT]
        val dx = mouseX + stick.x + if (keys[Key.LEFT]) -0.5 else if (keys[Key.RIGHT]) 0.5 else 0.0
        val dy = mouseY + stick.y + if (keys[Key.DOWN]) -0.5 else if (keys[Key.UP]) 0.5 else 0.0
        return dx.coerceIn(-1.0, 1.0) to dy.coerceIn(-1.0, 1.0)
    }
}
