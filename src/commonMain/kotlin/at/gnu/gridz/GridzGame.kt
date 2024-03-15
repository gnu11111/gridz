package at.gnu.gridz

import kotlin.math.*

class GridzGame : GridzInput {

    var x = WIDTH / 2.0
        private set
    var y = HEIGHT / 2.0
        private set
    var distance = 0.0
        private set
    var angle = 0.0
        private set
    val level = GridzLevel()

    private var speed = 0.0

    override fun tick(inputX: Double, inputY: Double, dt: Float) {
        distance = sqrt((inputX * inputX) + (inputY * inputY)).coerceAtMost(1.0)
        if (distance > 0.0) angle = atan2(inputX, inputY)
        val (dx, dy) = updateSpeed(distance, angle, dt)
        updatePosition(dx, dy)
    }

    private fun updatePosition(dx: Double, dy: Double) {
        val tileX = (x / TILE_X).toInt().toDouble()
        val tileY = (y / TILE_Y).toInt().toDouble()
        val nextX = ((x + dx) / TILE_X)
        val nextY = ((y - dy) / TILE_Y)
        var ySnapped = false
        if (!isWall(nextX + 0.5 * sign(dx), tileY)) {
            if (isWall(nextX + 0.5 * sign(dx), tileY - sign(dy))) {
                y = ((tileY + 0.5) * TILE_Y.toDouble())
                ySnapped = true
            }
            x += dx
        }
        if (!isWall(tileX, nextY - 0.5 * sign(dy))) {
            if (isWall(tileX + sign(dx), nextY - 0.5 * sign(dy)) && !ySnapped)
                x = ((tileX + 0.5) * TILE_X.toDouble())
            y -= dy
        }
        if (x > WIDTH) x -= WIDTH
        if (x < 0) x += WIDTH
        if (y > HEIGHT) y -= HEIGHT
        if (y < 0) y += HEIGHT
    }

    private fun isWall(x: Double, y: Double): Boolean =
        (level.layout[y.toInt()][x.toInt()] != ' ')

    private fun updateSpeed(distance: Double, angle: Double, dt: Float): Pair<Double, Double> {
        val factor = dt * 0.5
        speed = if (distance != 0.0)
            (speed + (factor * distance)).coerceIn(-(factor * 10), factor * 10)
        else if (speed > 0.0)
            (speed - (factor * 5.0)).coerceAtLeast(0.0)
        else
            (speed + (factor * 5.0)).coerceAtMost(0.0)
        return speed * sin(angle) to speed * cos(angle)
    }

    companion object {
        const val NAME = "gridZ"
        const val WIDTH = 480
        const val HEIGHT = 480
        const val COLS = 20
        const val ROWS = 20
        const val TILE_X = WIDTH / COLS
        const val TILE_Y = HEIGHT / ROWS
    }
}
