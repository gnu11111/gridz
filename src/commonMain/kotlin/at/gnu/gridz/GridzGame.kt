package at.gnu.gridz

import at.gnu.gridz.levels.EmptyLevel
import at.gnu.gridz.levels.TestLevel
import at.gnu.gridz.levels.PacmanLevel
import at.gnu.gridz.levels.PortalsLevel
import korlibs.time.DateTime
import kotlin.math.*

class GridzGame : GridzInput {

    enum class State { INIT, LOADED, RUNNING, PAUSED, ENDED, UNKNOWN }

    private val levels = listOf(TestLevel(), PacmanLevel(), EmptyLevel(), PortalsLevel())

    var direction = 0.0
        private set
    var acceleration = 0.0
        private set
    var level = levels.first()
        private set
    var state = State.UNKNOWN
        private set
    var tiles: ArrayList<ArrayList<GridzTile>> = arrayListOf()
        private set
    var tileWidth = 0
        private set
    var tileHeight = 0
        private set
    var timer = 0L
        private set
    var x = 0.0
        private set
    var y = 0.0
        private set
    var levelNumber = 0
        private set

    private var preferX = true
    private var speed = 0.0
    private var start = 0L
    private var startPause = 0L
    private var pauseTime = 0L
    private var action: GridzAction = NoAction


    init {
        init(0)
    }

    override fun reset() {
        init(levelNumber)
    }

    override fun next(): TestLevel {
        val next = (levelNumber + 1) % levels.size
        init(next)
        return levels[next]
    }

    override fun previous(): TestLevel {
        val previous = (levelNumber + levels.size - 1) % levels.size
        init(previous)
        return levels[previous]
    }

    override fun pause(): State {
        if (state == State.RUNNING) {
            state = State.PAUSED
            startPause = DateTime.nowUnixMillisLong()
        } else if (state == State.PAUSED) {
            state = State.RUNNING
            pauseTime += DateTime.nowUnixMillisLong() - startPause
        }
        return state
    }

    override fun tick(inputX: Double, inputY: Double): List<GridzEvent> {
        if (state == State.PAUSED)
            return listOf(NothingHappened)
        else if (state == State.LOADED) {
            if ((inputX != 0.0) || (inputY != 0.0)) {
                state = State.RUNNING
                start = DateTime.nowUnixMillisLong()
            } else
                return listOf(NothingHappened)
        }
        val lastTimer = timer
        timer = DateTime.nowUnixMillisLong() - start - pauseTime
        val dt = timer - lastTimer
        if (action != NoAction) {
            val (state, x, y) = action.perform(dt)
            if (state == GridzAction.State.FINISHED)
                action = NoAction
            this.x = x
            this.y = y
            return listOf(NothingHappened)
        }
        acceleration = sqrt((inputX * inputX) + (inputY * inputY)).coerceAtMost(1.0)
        direction = atan2(inputX, inputY)
        val (dx, dy) = updateSpeed(dt)
        val tile = updatePosition(dx, dy)
        val events = mutableListOf<GridzEvent>()
        if (tile != null) {
            events += TileEntered(tile)
            when (tile) {
                is Exit -> {
                    x = (tile.x + 0.5) * tileWidth
                    y = (tile.y + 0.5) * tileHeight
                    state = State.ENDED
                    return events + GameEnded
                }
                is Empty -> tile.lit = Empty.LIT_TIME
                is Portal -> {
                    tiles.firstNotNullOfOrNull { row ->
                        row.firstOrNull { (it !== tile) && (it is Portal) && (it.id == tile.id) }
                    }?.let {
                        action = Teleport(this, tile.x, tile.y, it.x, it.y)
                        events += Teleporting(it)
                    }
                }
                is Wall -> reset()
            }
        }
        events += updateTiles(dt)
        return events
    }

    private fun init(levelNumber: Int) {
        state = State.INIT
        this.levelNumber = levelNumber
        this.level = levels[levelNumber]
        pauseTime = 0L
        tileWidth = WIDTH / level.cols
        tileHeight = HEIGHT / level.rows
        x = tileWidth * (level.startX + 0.5)
        y = tileHeight * (level.startY + 0.5)
        acceleration = 0.0
        direction = 0.0
        timer = 0L
        tiles = arrayListOf()
        for (y in 0 until level.rows) {
            val row = arrayListOf<GridzTile>()
            for (x in 0 until level.cols) {
                val c = level.layout.getOrNull(y)?.getOrNull(x) ?: ' '
                val tile = when {
                    (c == '*') -> Wall(x, y)
                    (c == 'x') -> Exit(x, y)
                    c.isDigit() -> Portal(x, y, c.digitToInt())
                    else -> Empty(x, y)
                }
                row += tile
            }
            tiles += row
        }
        state = State.LOADED
    }

    private fun updateTiles(dt: Long): List<GridzEvent> {
        val events = mutableListOf<GridzEvent>()
        tiles.forEach { row -> row.forEach {
            if ((it is Empty) && (it.lit > 0L)) {
                it.lit = (it.lit - (dt * 10L)).coerceAtLeast(0L)
                if (it.lit <= 0L) events += TileLitDeceased(it)
            }
        } }
        return events
    }

    private fun updateSpeed(dt: Long): Pair<Double, Double> {
        val factor = dt * tileWidth / 500.0
        speed = if (acceleration != 0.0)
            (speed + (factor * acceleration)).coerceIn(0.0, tileWidth / 14.0)
        else
            (speed - (factor * 1.0)).coerceAtLeast(0.0)
        val dx = speed * sin(direction)
        val dy = speed * cos(direction)
        return (if (abs(dx) < 0.1) 0.0 else dx) to (if (abs(dy) < 0.1) 0.0 else dy)
    }

    private fun updatePosition(dx: Double, dy: Double): GridzTile? {
        val thisX = ((x / tileWidth)).toInt()
        val thisY = ((y / tileHeight)).toInt()
        val snapX = (thisX + 0.5) * tileWidth
        val snapY = (thisY + 0.5) * tileHeight
        val nextXDouble = (((x + dx) / tileWidth) + 0.5 * sign(dx))
        val nextX = if (nextXDouble >= 0.0) nextXDouble.toInt() else level.cols - 1
        val nextYDouble = (((y - dy) / tileHeight) - 0.5 * sign(dy))
        val nextY = if (nextYDouble >= 0.0) nextYDouble.toInt() else level.rows - 1
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
        return if ((endX != thisX) || (endY != thisY)) tile(endX, endY) else null
    }

    private fun tile(x: Int, y: Int): GridzTile? =
        tiles.getOrNull((y + level.rows) % level.rows)?.getOrNull((x + level.cols) % level.cols)

    private fun isWall(x: Int, y: Int): Boolean =
        tile(x, y) is Wall


    companion object {
        const val NAME = "gridZ"
        const val WIDTH = 480
        const val HEIGHT = 480
    }
}
