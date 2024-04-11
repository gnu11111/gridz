package at.gnu.gridz

sealed interface GridzEvent

data object NothingHappened : GridzEvent

data object GameEnded : GridzEvent

class TileEntered(val tile: GridzTile) : GridzEvent

class TileLitDeceased(val tile: GridzTile) : GridzEvent

class StartTeleporting(val from: GridzTile, val to: GridzTile) : GridzEvent

class CollectedItem(val item: GridzItem) : GridzEvent

class ConsumedItem(val item: GridzItem) : GridzEvent

data object EndTeleporting : GridzEvent
