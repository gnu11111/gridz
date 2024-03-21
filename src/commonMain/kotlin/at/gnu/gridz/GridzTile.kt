package at.gnu.gridz

class GridzTile(val x: Int, val y: Int, val type: TileType = TileType.EMPTY, var lit: Boolean = false) {

    enum class TileType {
        EMPTY, WALL
    }
}
