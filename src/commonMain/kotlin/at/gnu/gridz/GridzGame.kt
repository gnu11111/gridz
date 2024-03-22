package at.gnu.gridz

import at.gnu.gridz.GridzTile.Companion.LIFETIME
import kotlin.math.*

class GridzGame : GridzInput {

    var x = 0.0
        private set
    var y = 0.0
        private set
    var distance = 0.0
        private set
    var angle = 0.0
        private set
    var tiles: ArrayList<ArrayList<GridzTile>> = arrayListOf()
        private set

    var tileWidth = 0
    var tileHeight = 0
    private var speed = 0.0
    private var preferX = true


    init {
        init(GridzLevel())
    }

    private fun init(level: GridzLevel) {
        tileWidth = WIDTH / level.cols
        tileHeight = HEIGHT / level.rows
        x = (tileWidth * level.startX).toDouble()
        y = (tileHeight * level.startY).toDouble()
        distance = 0.0
        angle = 0.0
        tiles = arrayListOf()
        for (y in 0 until level.rows) {
            val row = arrayListOf<GridzTile>()
            for (x in 0 until level.cols) {
                val type = when (level.layout.getOrNull(y)?.getOrNull(x)) {
                    '*' -> GridzTile.TileType.WALL
                    else -> GridzTile.TileType.EMPTY
                }
                row += GridzTile(x, y, type)
            }
            tiles += row
        }
    }

    override fun tick(inputX: Double, inputY: Double, dt: Float) {
        distance = sqrt((inputX * inputX) + (inputY * inputY)).coerceAtMost(1.0)
        if (distance > 0.0) angle = atan2(inputX, inputY)
        val (dx, dy) = updateSpeed(dt)
        updatePosition(dx, dy)
        updateTiles()
    }

    private fun updateTiles() {
        tiles.forEach { row -> row.forEach { if (it.lit > 0) it.lit-- } }
    }

    private fun updateSpeed(dt: Float): Pair<Double, Double> {
        val factor = dt * tileWidth / 50.0
        speed = if (distance != 0.0)
            (speed + (factor * distance)).coerceIn(0.0, factor * 7.0)
        else
            (speed - (factor * 1.0)).coerceAtLeast(0.0)
        val dx = speed * sin(angle)
        val dy = speed * cos(angle)
        return (if (abs(dx) < 0.1) 0.0 else dx) to (if (abs(dy) < 0.1) 0.0 else dy)
    }

    private fun updatePosition(dx: Double, dy: Double) {
        val thisX = ((x / tileWidth)).toInt()
        val thisY = ((y / tileHeight)).toInt()
        val snapX = (thisX + 0.5) * tileWidth
        val snapY = (thisY + 0.5) * tileHeight
        val nextX = (((x + dx) / tileWidth) + 0.5 * sign(dx)).toInt()
        val nextY = (((y - dy) / tileHeight) - 0.5 * sign(dy)).toInt()
        val offsetX = (x / tileWidth) - (x / tileWidth).toInt()
        val offsetY = (y / tileHeight) - (y / tileHeight).toInt()
        val speedX = speed * sign(dx)
        val speedY = speed * sign(dy)

        if ((nextX == thisX) && (nextY != thisY)) {
            if (!isWall(thisX, nextY) && ((offsetX < 0.5) && (isWall(thisX - 1, nextY)))) {
                if (abs(0.5 - offsetX) < 0.1) x = snapX else x += speedX + 1.0
                y = snapY
            } else if (!isWall(thisX, nextY) && ((offsetX > 0.5) && (isWall(thisX + 1, nextY)))) {
                if (abs(0.5 - offsetX) < 0.1) x = snapX else x += speedX - 1.0
                y = snapY
            } else if (isWall(thisX, nextY)) {
                preferX = false
                x += speedX
            } else {
                x += dx
                y -= dy
            }
        } else if ((nextX != thisX) && (nextY == thisY)) {
            if (!isWall(nextX, thisY) && ((offsetY > 0.5) && (isWall(nextX, thisY + 1)))) {
                x = snapX
                if (abs(0.5 - offsetY) < 0.1) y = snapY else y -= speedY + 1.0
            } else if (!isWall(nextX, thisY) && ((offsetY < 0.5) && (isWall(nextX, thisY - 1)))) {
                x = snapX
                if (abs(0.5 - offsetY) < 0.1) y = snapY else y -= speedY - 1.0
            } else if (isWall(nextX, thisY)) {
                preferX = true
                y -= speedY
            } else {
                x += dx
                y -= dy
            }
        } else if ((nextX != thisX) && (nextY != thisY)) {
            preferX = if (abs(dx) > (2.0 * abs(dy))) true else if (abs(dy) > (2.0 * abs(dx))) false else preferX
            if (!isWall(nextX, nextY) && isWall(nextX, thisY) && isWall(thisX, nextY)) {
                // do nothing
            } else if (isWall(nextX, nextY) && !isWall(nextX, thisY) && preferX) {
                x += speedX
                y = snapY
            } else if (isWall(nextX, nextY) && !isWall(thisX, nextY) && preferX) {
                x = snapX
                y -= speedY
            } else if (isWall(nextX, nextY) && !isWall(thisX, nextY) && !preferX) {
                x = snapX
                y -= speedY
            } else if (isWall(nextX, nextY) && !isWall(nextX, thisY) && !preferX) {
                x += speedX
                y = snapY
            } else if (!isWall(nextX, nextY) && isWall(thisX, nextY)) {
                x += speedX
                y = snapY
            } else if (!isWall(nextX, nextY) && isWall(nextX, thisY)) {
                x = snapX
                y -= speedY
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

        val endX = ((x / tileWidth)).toInt()
        val endY = ((y / tileHeight)).toInt()
        val tile = tile(endX, endY)
        if (((endX != thisX) || (endY != thisY)) && (tile != GridzTile.OUT_OF_BOUNDS))
            tile.lit = LIFETIME
    }

    private fun tile(x: Int, y: Int): GridzTile =
        tiles.getOrNull(y)?.getOrNull(x) ?: GridzTile.OUT_OF_BOUNDS

    private fun isWall(x: Int, y: Int): Boolean =
        (tile(x, y).type == GridzTile.TileType.WALL)

    companion object {
        const val NAME = "gridZ"
        const val WIDTH = 480
        const val HEIGHT = 480
    }
}
