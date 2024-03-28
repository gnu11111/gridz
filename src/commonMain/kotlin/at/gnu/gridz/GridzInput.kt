package at.gnu.gridz

interface GridzInput {

    fun reset()
    fun pause(): GridzGame.State
    fun tick(inputX: Double, inputY: Double, dt: Float)
}
