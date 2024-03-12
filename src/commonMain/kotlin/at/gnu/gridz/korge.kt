package at.gnu.gridz

import at.gnu.gridz.GridzGame.Companion.COLS
import at.gnu.gridz.GridzGame.Companion.HEIGHT
import at.gnu.gridz.GridzGame.Companion.ROWS
import at.gnu.gridz.GridzGame.Companion.WIDTH
import at.gnu.gridz.GridzRenderer.Companion.SCORE_WIDTH
import korlibs.event.GameStick
import korlibs.event.Key
import korlibs.image.color.Colors
import korlibs.korge.Korge
import korlibs.korge.scene.ScaledScene
import korlibs.korge.scene.sceneContainer
import korlibs.korge.view.*
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size
import korlibs.math.isEven
import kotlin.math.cos
import kotlin.math.sin

class GridzRenderer {

    suspend fun init(gridzGame: GridzGame) {
        Korge(
            title = GridzGame.NAME,
            windowSize = Size(WIDTH + SCORE_WIDTH, HEIGHT),
            virtualSize = Size(WIDTH + SCORE_WIDTH, HEIGHT),
            backgroundColor = Colors[BACKGROUND_COLOR]
        ) {
            sceneContainer().changeTo { GameScene(gridzGame) }
        }
    }

    companion object {
        const val BACKGROUND_COLOR = "#2b0000"
        const val SCORE_WIDTH = 160
    }
}


class GameScene(private val game: GridzGame) : ScaledScene(WIDTH + SCORE_WIDTH, HEIGHT) {

    private val tileWidth = WIDTH / COLS
    private val tileHeight = HEIGHT / ROWS

    override suspend fun SContainer.sceneInit() {
        for (y in 0 until ROWS)
            for (x in 0 until COLS) {
                val color = if ((x + y).isEven) Colors["#1d1d1d"] else Colors["#1a1a1a"]
                solidRect(tileWidth, tileWidth, color) { position(x * tileWidth, y * tileHeight) }
            }

        for ((y, row) in game.level.layout.split("\n").withIndex()) {
            for ((x, c) in row.withIndex()) {
                if (c != '*') continue
                roundRect(Size(tileWidth - 2, tileHeight - 2), RectCorners(4), Colors.BLUE, Colors.DARKCYAN, 4) {
                    position(x * tileWidth + 1, y * tileHeight + 1)
                }
            }
        }
    }

    override suspend fun SContainer.sceneMain() {
        val player = circle(
            radius = tileWidth / 2.4,
            fill = Colors.DARKRED,
            stroke = Colors.ORANGE,
            strokeThickness = tileWidth * 0.2
        ) {
            anchor(0.4, 0.4)
        }
        val direction = circle(radius = tileWidth / 6, fill = Colors.ANTIQUEWHITE) { anchor(0.5, 0.5) }
        solidRect(SCORE_WIDTH, HEIGHT, Colors.DIMGREY) { position(WIDTH, 0) }

        addUpdater { dt ->
            val (dx, dy) = getInput()
            game.tick(dx, dy, dt.inWholeMilliseconds)
            val directionRadius = game.distance * player.radius * 0.8
            val directionX = game.x + (directionRadius * 0.6 * sin(game.angle))
            val directionY = game.y - (directionRadius * 0.6 * cos(game.angle))
            player.position(game.x, game.y)
            direction.position(directionX, directionY)
        }
    }

    private fun getInput(): Pair<Double, Double> {
        val stick = input.gamepads[0][GameStick.LEFT]
        val dx = stick.x + if (keys[Key.LEFT]) -1.0 else if (keys[Key.RIGHT]) 1.0 else 0.0
        val dy = stick.y + if (keys[Key.DOWN]) -1.0 else if (keys[Key.UP]) 1.0 else 0.0
        return dx.coerceIn(-1.0, 1.0) to dy.coerceIn(-1.0, 1.0)
    }
}
