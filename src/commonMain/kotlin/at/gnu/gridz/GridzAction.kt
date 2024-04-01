package at.gnu.gridz

sealed class GridzAction {
    enum class State { RUNNING, FINISHED }

    open fun perform(dt: Long): Triple<State, Double, Double> {
        return Triple(State.RUNNING, 0.0, 0.0)
    }
}

data object NoAction : GridzAction()

class Teleport(game: GridzGame, fromX: Int, fromY: Int, toX: Int, toY: Int) : GridzAction() {

    private var time = 0L
    private var startX = game.x
    private var startY = game.y
    private val fromX = (fromX + 0.5) * game.tileWidth
    private val fromY = (fromY + 0.5) * game.tileHeight
    private val toX = (toX + 0.5) * game.tileWidth
    private val toY = (toY + 0.5) * game.tileHeight

    override fun perform(dt: Long): Triple<State, Double, Double> {
        time += dt
        val x: Double
        val y: Double
        if (time < 100L) {
            x = startX + time * (fromX - startX) / 100.0
            y = startY + time * (fromY - startY) / 100.0
            return Triple(State.RUNNING, x, y)
        } else if (time < 300L) {
            x = fromX + (time - 100L) * (toX - fromX) / 200.0
            y = fromY + (time - 100L) * (toY - fromY) / 200.0
            return Triple(State.RUNNING, x, y)
        } else if (time < 350L) {
            return Triple(State.RUNNING, toX, toY)
        }
        return Triple(State.FINISHED, toX, toY)
    }
}
