package at.gnu.gridz

import at.gnu.gridz.levels.SpaceLevel
import at.gnu.gridz.levels.GridzLevel
import at.gnu.gridz.levels.PacmanLevel
import at.gnu.gridz.levels.PortalsLevel
import korlibs.logger.Console
import korlibs.time.DateTime
import kotlin.math.*

class GridzGame : GridzHandler {

    enum class State { INIT, LOADED, RUNNING, ENDED, PAUSED, UNKNOWN }

    private val levels = listOf(GridzLevel(), PacmanLevel(), SpaceLevel(), PortalsLevel())

    var direction = 0.0f
        private set
    var acceleration = 0.0f
        private set
    var items: ArrayList<GridzItem> = arrayListOf()
        private set
    var inventory: ArrayList<GridzItem> = arrayListOf()
        private set
    var level = levels.first()
        private set
    var levelNumber = 0
        private set
    var state = State.UNKNOWN
        private set
    var tasks: MutableMap<String, Int> = mutableMapOf()
        private set
    var tiles: ArrayList<GridzTile> = arrayListOf()
        private set
    var timer = 0L
        private set
    var x = 0.0f
        private set
    var y = 0.0f
        private set
    var chargeTile = ChargeTile()
        private set

    private var preferX = true
    private var levelCompleted = false
    private var speed = 0.0f
    private var start = 0L
    private var startPause = 0L
    private var pauseTime = 0L
    private var timeSinceLastMoved = 0L
    private var action: GridzAction = NoAction


    init {
        init(0)
    }

    override fun reset() {
        init(levelNumber)
    }

    override fun next(): GridzLevel {
        val next = (levelNumber + 1) % levels.size
        init(next)
        return levels[next]
    }

    override fun previous(): GridzLevel {
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

    override fun tick(inputX: Float, inputY: Float): List<GridzEvent> {
        if (state == State.LOADED) {
            if ((inputX != 0.0f) || (inputY != 0.0f)) {
                state = State.RUNNING
                start = DateTime.nowUnixMillisLong()
            } else
                return emptyList()
        } else if (state != State.RUNNING)
            return emptyList()
        val lastTimer = timer
        timer = DateTime.nowUnixMillisLong() - start - pauseTime
        val dt = timer - lastTimer
        if (checkTimeout(inputX, inputY, dt))
            return listOf(GameReset)
        when (val event = handleAction(dt)) {
            is WallMoved -> return event.toTile.handleItems() + event.toTile.onEntered() + event
            is TeleportEnded -> return handleTasks(Teleport.NAME) + TeleportEnded
            is GameEnded -> {
                state = State.ENDED
                return listOf(GameEnded)
            }
            is ActionInProgress -> return listOf(ActionInProgress)
            else -> { }
        }
        val (dx, dy) = updateSpeed(inputX, inputY, dt)
        val (nextTile, tile) = updatePosition(dx, dy)
        return tile.handleItems() + tile.onEntered() + updateTiles(dt) + nextTile.updateCharging(dt)
    }

    private fun handleAction(dt: Long): GridzEvent? {
        if (action == NoAction)
            return null
        val (event, x, y) = action.perform(dt)
        this.x = x
        this.y = y
        return if (event != null) {
            action = NoAction
            event
        } else if ((action is EnterExit) || (action is Teleport) || (action is MoveWall))
            ActionInProgress
        else
            null
    }

    private fun handleTasks(name: String): List<GridzEvent> {
        if (!levelCompleted) {
            if (tasks.contains(name))
                tasks[name] = tasks[name]!!.minus(1).coerceAtLeast(0)
            if (tasks.areCompleted()) {
                levelCompleted = true
                tiles.forEach { if (it is Exit) it.open = true }
                return listOf(ExitOpened)
            }
        }
        return emptyList()
    }

    private fun GridzTile?.handleItems(): MutableList<GridzEvent> {
        val events = mutableListOf<GridzEvent>()
        if (this == null) return events
        val item = item(this.x, this.y) ?: return events
        items -= item
        events += handleTasks(item.name)
        if (item.collectable && (inventory.size < level.maxInventory)) {
            inventory += item
            events += ItemCollected(item)
        } else
            events += ItemConsumed(item)
        return events
    }

    private fun GridzTile?.onEntered(): List<GridzEvent> {
        if (this == null)
            return emptyList()
        val events = mutableListOf<GridzEvent>()
        when (this) {
            is Exit -> {
                action = EnterExit(this@GridzGame.x, this@GridzGame.y, x + 0.5f, y + 0.5f, GameEnded)
                events += ExitEntered(this)
            }
            is Empty -> if (level.tailLitTime > 0L) {
                if (lit <= 0L) events += handleTasks(Empty.NAME)
                lit = level.tailLitTime
                events += TileLit(this)
            }
            is Portal -> tiles.firstOrNull {
                (it !== this) && (it is Portal) && (it.id == id)
            }?.let {
                action = Teleport(this@GridzGame.x, this@GridzGame.y, x, y, it.x, it.y, TeleportEnded)
                events += TeleportStarted(this, it)
            }
            is Wall -> events += GameReset.also { Console.error("Stuck in wall!") }
        }
        return events.ifEmpty { listOf(TileEntered) }
    }

    private fun checkTimeout(inputX: Float, inputY: Float, dt: Long): Boolean {
        if ((inputX != 0.0f) && (inputY != 0.0f))
            timeSinceLastMoved = 0L
        else {
            timeSinceLastMoved += dt
            if (timeSinceLastMoved > TIMEOUT_NOT_MOVING)
                return true
        }
        return false
    }

    private fun init(levelNumber: Int) {
        state = State.INIT
        this.levelNumber = levelNumber
        this.level = levels[levelNumber]
        pauseTime = 0L
        timeSinceLastMoved = 0L
        x = level.startX + 0.5f
        y = level.startY + 0.5f
        acceleration = 0.0f
        direction = 0.0f
        timer = 0L
        tiles = arrayListOf()
        items = arrayListOf()
        inventory = arrayListOf()
        tasks = level.tasks.toMutableMap()
        levelCompleted = false
        chargeTile = ChargeTile()
        for (y in 0 until level.rows) {
            for (x in 0 until level.cols) {
                val c = level.layout.getOrNull(y)?.getOrNull(x) ?: ' '
                when  {
                    c.isDigit() -> tiles += Portal(x, y, c.digitToInt())
                    (c == '*') -> tiles += Wall(x, y)
                    (c == 'x') -> tiles += Exit(x, y)
                    else -> tiles += Empty(x, y)
                }
                when (c) {
                    'k' -> items += Key(x, y)
                    '.' -> items += Pill(x, y)
                }
            }
        }
        state = State.LOADED
    }

    private fun updateTiles(dt: Long): List<GridzEvent> {
        val events = mutableListOf<GridzEvent>()
        tiles.forEach {
            if ((it is Empty) && (it.lit > 0L)) {
                it.lit = (it.lit - (dt * 10L)).coerceAtLeast(0L)
                if (it.lit <= 0L) events += TileLitDeceased(it)
            }
        }
        return events
    }

    private fun updateSpeed(inputX: Float, inputY: Float, dt: Long): Pair<Float, Float> {
        acceleration = sqrt((inputX * inputX) + (inputY * inputY)).coerceAtMost(1.0f)
        if ((abs(inputX) + abs(inputY))> 0.004f)
            direction = atan2(inputX, inputY)
        val factor = dt / 500.0f
        speed = if (acceleration != 0.0f)
            (speed + (factor * acceleration)).coerceIn(0.0f, factor * 5.0f)
        else
            (speed - factor).coerceAtLeast(0.0f)
        val dx = (speed * sin(direction)).coerceIn(-0.5f, 0.5f)
        val dy = (speed * cos(direction)).coerceIn(-0.5f, 0.5f)
        return (if (abs(dx) < 0.004f) 0.0f else dx) to (if (abs(dy) < 0.004f) 0.0f else dy)
    }

    private fun updatePosition(dx: Float, dy: Float): Pair<GridzTile?, GridzTile?> {
        val thisX = x.toInt()
        val thisY = y.toInt()
        val snapX = thisX + 0.5f
        val snapY = thisY + 0.5f
        val nextXFloat = x + dx + (0.5f * sign(dx))
        val nextX = if (nextXFloat >= 0.0f) nextXFloat.toInt() else level.cols - 1
        val nextYFloat = y - dy - (0.5f * sign(dy))
        val nextY = if (nextYFloat >= 0.0f) nextYFloat.toInt() else level.rows - 1
        val offsetX = x - x.toInt()
        val offsetY = y - y.toInt()
        val speedX = (speed * sign(dx)).coerceIn(-0.5f, 0.5f)
        val speedY = (speed * sign(dy)).coerceIn(-0.5f, 0.5f)

        if ((nextX == thisX) && (nextY != thisY)) {
            if (!isBlocked(thisX, nextY) && ((offsetX < 0.5f) && (isBlocked(thisX - 1, nextY)))) {
                if (abs(0.5f - offsetX) < 0.1f) x = snapX else x += speedX.coerceAtLeast(0.04f)
                y = snapY
            } else if (!isBlocked(thisX, nextY) && ((offsetX > 0.5f) && (isBlocked(thisX + 1, nextY)))) {
                if (abs(0.5f - offsetX) < 0.1f) x = snapX else x += speedX.coerceAtMost(-0.04f)
                y = snapY
            } else if (isBlocked(thisX, nextY)) {
                preferX = false
                x += speedX
            } else {
                x += dx
                y -= dy
            }
        } else if ((nextX != thisX) && (nextY == thisY)) {
            if (!isBlocked(nextX, thisY) && ((offsetY > 0.5f) && (isBlocked(nextX, thisY + 1)))) {
                x = snapX
                if (abs(0.5f - offsetY) < 0.1f) y = snapY else y -= speedY.coerceAtLeast(0.04f)
            } else if (!isBlocked(nextX, thisY) && ((offsetY < 0.5f) && (isBlocked(nextX, thisY - 1)))) {
                x = snapX
                if (abs(0.5f - offsetY) < 0.1f) y = snapY else y -= speedY.coerceAtMost(-0.04f)
            } else if (isBlocked(nextX, thisY)) {
                preferX = true
                y -= speedY
            } else {
                x += dx
                y -= dy
            }
        } else if ((nextX != thisX) && (nextY != thisY)) {
            preferX = if (abs(dx) > (2.0f * abs(dy))) true else if (abs(dy) > (2.0f * abs(dx))) false else preferX
            if (!isBlocked(nextX, nextY) && isBlocked(nextX, thisY) && isBlocked(thisX, nextY)) {
                // do nothing
            } else if (isBlocked(nextX, nextY) && !isBlocked(nextX, thisY) && preferX) {
                x += speedX
                y = snapY
            } else if (isBlocked(nextX, nextY) && !isBlocked(thisX, nextY) && preferX) {
                x = snapX
                y -= speedY
            } else if (isBlocked(nextX, nextY) && !isBlocked(thisX, nextY) && !preferX) {
                x = snapX
                y -= speedY
            } else if (isBlocked(nextX, nextY) && !isBlocked(nextX, thisY) && !preferX) {
                x += speedX
                y = snapY
            } else if (!isBlocked(nextX, nextY) && isBlocked(thisX, nextY)) {
                x += speedX
                y = snapY
            } else if (!isBlocked(nextX, nextY) && isBlocked(nextX, thisY)) {
                x = snapX
                y -= speedY
            } else if (!isBlocked(nextX, nextY)) {
                x += dx
                y -= dy
            }
        } else {
            x += dx
            y -= dy
        }

        if (x > level.cols) x -= level.cols
        if (x < 0.0f) x += level.cols
        if (y > level.rows) y -= level.rows
        if (y < 0.0f) y += level.rows

        val endX = x.toInt()
        val endY = y.toInt()
        val nextTile = tile(nextX, nextY)
        return if ((endX != thisX) || (endY != thisY)) nextTile to tile(endX, endY) else nextTile to null
    }

    private fun GridzTile?.updateCharging(dt: Long): List<GridzEvent> {
        val events = mutableListOf<GridzEvent>()
        if (this == null)
            return events
        else if (!isBlocked(x, y))
            chargeTile.charge = -1L
        else {
            if ((x == chargeTile.x) && (y == chargeTile.y)) {
                if (!chargeTile.charged) {
                    chargeTile.charge += dt
                    if (chargeTile.charge > 255L) {
                        val dx = sign((x - this@GridzGame.x.toInt()).toDouble()).toInt()
                        val dy = sign((y - this@GridzGame.y.toInt()).toDouble()).toInt()
                        val toTile = this.isMovable(dx, dy)
                        if (toTile != null) {
                            events += WallMoving(this, toTile)
                            action = MoveWall(this@GridzGame.x, this@GridzGame.y, this, toTile,
                                WallMoved(this, toTile))
                        }
                        chargeTile.charged = true
                    }
                }
            } else {
                chargeTile.x = x
                chargeTile.y = y
                chargeTile.charge = 0L
                chargeTile.charged = false
            }
        }
        return events
    }

    private fun tile(x: Int, y: Int): GridzTile? =
        tiles.firstOrNull { (it.x == ((x + level.cols) % level.cols)) && (it.y == ((y + level.rows) % level.rows)) }

    private fun item(x: Int, y: Int): GridzItem? =
        items.firstOrNull { (it.x == x) && (it.y == y) }

    private fun isBlocked(x: Int, y: Int): Boolean {
        val tile = tile(x, y)
        return tile is Wall || ((tile is Exit) && !tile.open)
    }

    private fun GridzTile.isMovable(dx: Int, dy: Int): GridzTile? {
        if (this !is Wall || ((dx != 0) && (dy != 0)))
            return null
        if (((x + dx) < 0) || ((x + dx) >= level.cols) || ((y + dy) < 0) || ((y + dy) >= level.rows))
            return null
        val tileBehind = tile(x + dx, y + dy) ?: return null
        return if (tileBehind is Empty) tileBehind else null
    }

    private fun Map<String, Int>.areCompleted(): Boolean =
        isEmpty() || all { it.value <= 0 }

    companion object {
        const val NAME = "gridZ"
        const val TIMEOUT_NOT_MOVING = 60000L
    }
}
