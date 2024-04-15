package at.gnu.gridz

sealed interface GridzEvent

data object NothingHappened : GridzEvent

data object GameEnded : GridzEvent

data object GameReset : GridzEvent

class TileLit(val tile: GridzTile) : GridzEvent

class TileLitDeceased(val tile: GridzTile) : GridzEvent

class TeleportStarted(val from: GridzTile, val to: GridzTile) : GridzEvent

class ItemCollected(val item: GridzItem) : GridzEvent

class ItemConsumed(val item: GridzItem) : GridzEvent

data object ExitOpened : GridzEvent

data object TeleportEnded : GridzEvent
