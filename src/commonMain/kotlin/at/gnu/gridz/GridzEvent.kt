package at.gnu.gridz

sealed interface GridzEvent

data object NothingHappened : GridzEvent

class TileEntered(val tile: GridzTile) : GridzEvent

class TileLitDeceased(val tile: GridzTile) : GridzEvent
