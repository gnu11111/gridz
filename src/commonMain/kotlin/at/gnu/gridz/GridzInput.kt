package at.gnu.gridz

import at.gnu.gridz.levels.TestLevel

interface GridzInput {

    fun reset()
    fun pause(): GridzGame.State
    fun next(): TestLevel
    fun previous(): TestLevel
    fun tick(inputX: Double, inputY: Double): List<GridzEvent>
}
