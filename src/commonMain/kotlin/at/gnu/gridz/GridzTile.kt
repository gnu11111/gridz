package at.gnu.gridz

sealed class GridzTile(open val x: Int, open val y: Int)

class Empty(override val x: Int, override val y: Int) : GridzTile(x, y) {
    var lit: Long = 0L

    companion object {
        const val LIT_TIME = 50000L
    }
}

class Wall(override val x: Int, override val y: Int) : GridzTile(x, y)

class Exit(override val x: Int, override val y: Int) : GridzTile(x, y)

class Portal(override val x: Int, override val y: Int, val id: Int = 0) : GridzTile(x, y)
