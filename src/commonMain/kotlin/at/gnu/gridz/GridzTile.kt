package at.gnu.gridz

sealed class GridzTile(open val x: Int, open val y: Int)

class Empty(override var x: Int, override var y: Int) : GridzTile(x, y) {
    var lit = 0L

    companion object {
        const val NAME = "Empty"
    }
}

class Wall(override var x: Int, override var y: Int) : GridzTile(x, y)

class Exit(override val x: Int, override val y: Int) : GridzTile(x, y) {
    var open = false
}

class Portal(override val x: Int, override val y: Int, val id: Int = 0) : GridzTile(x, y)

class ChargeTile(var x: Int = -1, var y: Int = -1, var charge: Long = 0L, var charged: Boolean = false)
