package at.gnu.gridz

interface GridzInput {

    fun tick(dx: Double, dy: Double, dt: Long): Double
}
