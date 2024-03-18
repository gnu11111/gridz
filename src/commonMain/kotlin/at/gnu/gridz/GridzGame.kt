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

    private var preferX = true


    private var speed = 0.0

    override fun tick(inputX: Double, inputY: Double, dt: Float) {
        if ((inputX == 0.0) && (inputY == 0.0)) return
        distance = sqrt((inputX * inputX) + (inputY * inputY)).coerceAtMost(1.0)
        if (distance > 0.0) angle = atan2(inputX, inputY)
        val (dx, dy) = updateSpeed(distance, angle, dt)
        updatePosition(dx, dy)
    }

    private fun updatePosition(dx: Double, dy: Double) {
        val thisX = ((x / TILE_WIDTH)).toInt()
        val thisY = ((y / TILE_HEIGHT)).toInt()
        val nextX = (((x + dx) / TILE_WIDTH) + 0.5 * sign(dx)).toInt()
        val nextY = (((y - dy) / TILE_HEIGHT) - 0.5 * sign(dy)).toInt()
        val snapX = (thisX + 0.5) * TILE_WIDTH
        val snapY = (thisY + 0.5) * TILE_HEIGHT
        val offsetX = (x / TILE_WIDTH) - (x / TILE_WIDTH).toInt()
        val offsetY = (y / TILE_HEIGHT) - (y / TILE_HEIGHT).toInt()

        if ((nextX == thisX) && (nextY != thisY)) {
            preferX = false
            if (!isWall(thisX, nextY) && ((offsetX > 0.5) && (isWall(thisX + 1, nextY)))) {
                x += dx - 0.5
                y = snapY
            } else if (!isWall(thisX, nextY) && ((offsetX < 0.5) && (isWall(thisX - 1, nextY)))) {
                x += dx + 0.5
                y = snapY
            } else if (isWall(thisX, nextY)) {
                x += dx
            } else {
                x += dx
                y -= dy
            }
        } else if ((nextX != thisX) && (nextY == thisY)) {
            preferX = true
            if (!isWall(nextX, thisY) && ((offsetY > 0.5) && (isWall(nextX, thisY + 1)))) {
                x = snapX
                y -= dy + 0.5
            } else if (!isWall(nextX, thisY) && ((offsetY < 0.5) && (isWall(nextX, thisY - 1)))) {
                x = snapX
                y -= dy - 0.5
            } else if (isWall(nextX, thisY)) {
                y -= dy
            } else {
                x += dx
                y -= dy
            }
        } else if ((nextX != thisX) && (nextY != thisY)) {
            if (isWall(nextX, nextY) && !isWall(nextX, thisY) && preferX) {
                x += dx
                y = snapY
            } else if (isWall(nextX, nextY) && !isWall(thisX, nextY) && preferX) {
                x = snapX
                y -= dy
            } else if (isWall(nextX, nextY) && !isWall(thisX, nextY) && !preferX) {
                x = snapX
                y -= dy
            } else if (isWall(nextX, nextY) && !isWall(nextX, thisY) && !preferX) {
                x += dx
                y = snapY
            } else if (!isWall(nextX, nextY) && isWall(thisX, nextY)) {
                x += dx
                y = snapY
            } else if (!isWall(nextX, nextY) && isWall(nextX, thisY)) {
                x = snapX
                y -= dy
            } else if (!isWall(nextX, nextY)) {
                x += dx
                y -= dy
            }
        } else {
            x += dx
            y -= dy
        }

        if (x > WIDTH) x -= WIDTH
        if (x < 0) x += WIDTH
        if (y > HEIGHT) y -= HEIGHT
        if (y < 0) y += HEIGHT
    }

    private fun isWall(x: Int, y: Int): Boolean =
        (level.layout[y][x] != ' ')

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
        const val TILE_WIDTH = WIDTH / COLS
        const val TILE_HEIGHT = HEIGHT / ROWS
    }
}
