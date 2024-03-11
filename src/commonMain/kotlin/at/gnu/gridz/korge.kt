package at.gnu.gridz

import at.gnu.gridz.GridzGame.Companion.HEIGHT
import at.gnu.gridz.GridzGame.Companion.WIDTH
import at.gnu.gridz.KorgeRenderer.Companion.SCORE_WIDTH
import korlibs.event.GameStick
import korlibs.image.color.Colors
import korlibs.korge.Korge
import korlibs.korge.scene.ScaledScene
import korlibs.korge.scene.sceneContainer
import korlibs.korge.view.*
import korlibs.math.geom.Size
import korlibs.math.isEven
import kotlinx.coroutines.cancel
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class KorgeRenderer : GridzRenderer {

    private lateinit var scene: GameScene

    override suspend fun init(gridzGame: GridzGame) {
        Korge(
            title = GridzGame.NAME,
            windowSize = Size(WIDTH + SCORE_WIDTH, HEIGHT),
            virtualSize = Size(WIDTH + SCORE_WIDTH, HEIGHT),
            backgroundColor = Colors[BACKGROUND_COLOR]
        ) {
            scene = sceneContainer().changeTo { GameScene(gridzGame) }
        }
    }

    override fun close() {
        scene.cancel("closed")
    }

    companion object {
        const val BACKGROUND_COLOR = "#2b2b2b"
        const val SCORE_WIDTH = 160
    }
}


class GameScene(private val game: GridzGame) : ScaledScene(WIDTH + SCORE_WIDTH, HEIGHT) {

    private val tileWidth = WIDTH / 20
    private val tileHeight = HEIGHT / 20

    override suspend fun SContainer.sceneInit() {
        for (y in 0..19)
            for (x in 0..19) {
                val color = if ((x + y).isEven) Colors["#202020"] else Colors["#151515"]
                solidRect(tileWidth, tileWidth, color) { position(x * tileWidth, y * tileHeight) }
            }
        solidRect(SCORE_WIDTH, HEIGHT, Colors.DIMGREY) { position(WIDTH, 0) }
    }

    override suspend fun SContainer.sceneMain() {
        val player = circle(
            radius = tileWidth / 2.4,
            fill = Colors.BLUE,
            stroke = Colors.CADETBLUE,
            strokeThickness = tileWidth * 0.2
        ) {
            anchor(0.4, 0.4)
        }
        val direction = circle(radius = tileWidth / 6, fill = Colors.RED) { anchor(0.5, 0.5) }

        addUpdater { dt ->
            val (dx, dy) = getInput()
            val distance = game.tick(dx, dy, dt.inWholeMilliseconds)
            val directionRadius = distance * player.radius * 0.8
            val directionAngle = atan2(dx, dy)
            val directionX = game.x + (directionRadius * 0.6 * sin(directionAngle))
            val directionY = game.y - (directionRadius * 0.6 * cos(directionAngle))
            player.position(game.x, game.y)
            direction.position(directionX, directionY)
        }
    }

    private fun getInput(): Pair<Double, Double> {
        val stick = input.gamepads[0][GameStick.LEFT]
        return stick.x to stick.y
    }
}
