package at.gnu.gridz

sealed class GridzAction {
    enum class State { RUNNING, FINISHED }

    open fun perform(dt: Long): Triple<State, Float, Float> {
        return Triple(State.RUNNING, 0.0f, 0.0f)
    }
}

data object NoAction : GridzAction()

class Teleport(game: GridzGame, fromX: Int, fromY: Int, toX: Int, toY: Int) : GridzAction() {

    private var time = 0L
    private var startX = game.x
    private var startY = game.y
    private val fromX = fromX + 0.5f
    private val fromY = fromY + 0.5f
    private val toX = toX + 0.5f
    private val toY = toY + 0.5f

    override fun perform(dt: Long): Triple<State, Float, Float> {
        time += dt
        val x: Float
        val y: Float
        if (time < 300L) {
            x = startX + time * (fromX - startX) / 300.0f
            y = startY + time * (fromY - startY) / 300.0f
            return Triple(State.RUNNING, x, y)
        } else if (time < 600L) {
            x = fromX + (time - 300L) * (toX - fromX) / 300.0f
            y = fromY + (time - 300L) * (toY - fromY) / 300.0f
            return Triple(State.RUNNING, x, y)
        } else if (time < 700L) {
            return Triple(State.RUNNING, toX, toY)
        }
        return Triple(State.FINISHED, toX, toY)
    }
}
