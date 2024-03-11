package at.gnu.gridz

import kotlin.math.sqrt

class GridzGame : GridzInput {

    var x = WIDTH / 2.0
        private set
    var y = HEIGHT / 2.0
        private set

    private var speed = 1.0

    override fun tick(dx: Double, dy: Double, dt: Long): Double {
        val distance = sqrt((dx * dx) + (dy * dy)).coerceAtMost(1.0)
        speed = if (distance > 0.0)
            (speed + (distance * dt * 4)).coerceAtMost(4.0)
        else
            (speed - (dt * 15)).coerceAtLeast(0.0)
        x += dx * speed
        y -= dy * speed
        if (x > WIDTH) x -= WIDTH
        if (x < 0) x += WIDTH
        if (y > HEIGHT) y -= HEIGHT
        if (y < 0) y += HEIGHT
        return distance
    }

    companion object {
        const val NAME = "gridZ"
        const val WIDTH = 480
        const val HEIGHT = 480
    }
}
