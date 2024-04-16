package at.gnu.gridz

import at.gnu.gridz.levels.GridzLevel

interface GridzHandler {

    fun reset()
    fun pause(): GridzGame.State
    fun next(): GridzLevel
    fun previous(): GridzLevel
    fun tick(inputX: Float, inputY: Float): List<GridzEvent>
}
