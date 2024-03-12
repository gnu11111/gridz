package at.gnu.gridz

import kotlin.math.atan2
import kotlin.math.sqrt

class GridzGame : GridzInput {

    var x = WIDTH / 2.0
        private set
    var y = HEIGHT / 2.0
        private set
    var distance = 0.0
        private set
    var angle = 0.0
        private set
    val level = GridzLevel()

    private var speedX = 0.0
    private var speedY = 0.0

    override fun tick(dx: Double, dy: Double, dt: Long) {
        distance = sqrt((dx * dx) + (dy * dy)).coerceAtMost(1.0)
        angle = atan2(dx, dy)
        speedX = if (dx != 0.0)
            (speedX + (((4.0 * dx) - speedX) * dt * 0.02)).coerceIn(-4.0, 4.0)
        else if (speedX > 0.0)
            (speedX - 0.2).coerceAtLeast(0.0)
        else
            (speedX + 0.2).coerceAtMost(0.0)
        speedY = if (dy != 0.0)
            (speedY + (((4.0 * dy) - speedY) * dt * 0.02)).coerceIn(-4.0, 4.0)
        else if (speedY > 0.0)
            (speedY - 0.2).coerceAtLeast(0.0)
        else
            (speedY + 0.2).coerceAtMost(0.0)
        x += speedX
        y -= speedY
        if (x > WIDTH) x -= WIDTH
        if (x < 0) x += WIDTH
        if (y > HEIGHT) y -= HEIGHT
        if (y < 0) y += HEIGHT
    }

    companion object {
        const val NAME = "gridZ"
        const val WIDTH = 480
        const val HEIGHT = 480
        const val COLS = 20
        const val ROWS = 20
    }
}
