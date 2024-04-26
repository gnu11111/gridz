package at.gnu.gridz

sealed class GridzAction(open val endEvent: GridzEvent? = null) {

    open fun perform(dt: Long): Triple<GridzEvent?, Float, Float> {
        return Triple(endEvent, 0.0f, 0.0f)
    }
}

data object NoAction : GridzAction()

class MoveWall(
    fromX: Float,
    fromY: Float,
    private val fromTile: GridzTile,
    private val toTile: GridzTile,
    override val endEvent: GridzEvent?
) : GridzAction() {

    private var time = 0L
    private var x = fromX
    private var y = fromY
    private var toX = fromTile.x + 0.5f
    private var toY = fromTile.y + 0.5f

    override fun perform(dt: Long): Triple<GridzEvent?, Float, Float> {
        if ((fromTile !is Wall) || (toTile !is Empty))
            return Triple(endEvent, x, y)
        time += dt
        return if (time < 500L)
            Triple(null, x + (time * (toX - x) / 500.0f), y + (time * (toY - y) / 500.0f))
        else {
            val tempX = fromTile.x
            val tempY = fromTile.y
            fromTile.x = toTile.x
            fromTile.y = toTile.y
            toTile.x = tempX
            toTile.y = tempY
            Triple(endEvent, toX, toY)
        }
    }

    companion object {
        const val NAME = "MoveWall"
    }
}

class EnterExit(
    fromX: Float,
    fromY: Float,
    private val toX: Float,
    private val toY: Float,
    override val endEvent: GridzEvent?
) : GridzAction(endEvent) {

    private var x = fromX
    private var y = fromY
    private var time = 0L

    override fun perform(dt: Long): Triple<GridzEvent?, Float, Float> {
        time += dt
        return if (time < 300L)
            Triple(null, x + (time * (toX - x) / 300.0f), y + (time * (toY - y) / 300.0f))
        else
            Triple(endEvent, toX, toY)
    }
}

class Teleport(
    private val startX: Float,
    private val startY: Float,
    fromX: Int,
    fromY: Int,
    toX: Int,
    toY: Int,
    override val endEvent: GridzEvent?
) : GridzAction(endEvent) {

    private var time = 0L
    private val fromX = fromX + 0.5f
    private val fromY = fromY + 0.5f
    private val toX = toX + 0.5f
    private val toY = toY + 0.5f

    override fun perform(dt: Long): Triple<GridzEvent?, Float, Float> {
        time += dt
        val x: Float
        val y: Float
        if (time < 300L) {
            x = startX + (time * (fromX - startX) / 300.0f)
            y = startY + (time * (fromY - startY) / 300.0f)
            return Triple(null, x, y)
        } else if (time < 600L) {
            x = fromX + ((time - 300L) * (toX - fromX) / 300.0f)
            y = fromY + ((time - 300L) * (toY - fromY) / 300.0f)
            return Triple(null, x, y)
        } else if (time < 700L) {
            return Triple(null, toX, toY)
        }
        return Triple(endEvent, toX, toY)
    }

    companion object {
        const val NAME = "Teleport"
    }
}
