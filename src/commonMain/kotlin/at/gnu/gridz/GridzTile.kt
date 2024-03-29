package at.gnu.gridz

import korlibs.korge.view.SolidRect

sealed class GridzTile(open val x: Int, open val y: Int) {
    var component: SolidRect? = null
}

class Empty(override val x: Int, override val y: Int) : GridzTile(x, y) {
    var lit: Int = 0

    companion object {
        const val LIT_TIME = 50000
    }
}

class Wall(override val x: Int, override val y: Int) : GridzTile(x, y)

class Exit(override val x: Int, override val y: Int) : GridzTile(x, y)

class Portal(override val x: Int, override val y: Int, val id: Int = 0) : GridzTile(x, y)
