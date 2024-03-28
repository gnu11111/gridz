package at.gnu.gridz

import korlibs.korge.view.SolidRect

class GridzTile(val x: Int, val y: Int, val type: TileType = TileType.EMPTY) {

    var lit: Int = 0
    var component: SolidRect? = null

    enum class TileType {
        EMPTY, WALL
    }

    companion object {
        const val LIFETIME = 50000
    }
}
