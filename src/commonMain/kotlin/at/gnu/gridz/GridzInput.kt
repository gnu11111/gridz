package at.gnu.gridz

interface GridzInput {

    fun resetLevel()
    fun tick(inputX: Double, inputY: Double, dt: Float)
}
