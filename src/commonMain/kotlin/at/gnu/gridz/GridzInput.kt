package at.gnu.gridz

import at.gnu.gridz.levels.GridzLevel

interface GridzInput {

    fun reset()
    fun pause(): GridzGame.State
    fun next(): GridzLevel
    fun previous(): GridzLevel
    fun tick(inputX: Double, inputY: Double, dt: Float): List<GridzEvent>
}
