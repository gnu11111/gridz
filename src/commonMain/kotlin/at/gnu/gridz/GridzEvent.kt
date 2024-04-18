package at.gnu.gridz

sealed interface GridzEvent

data object ActionInProgress : GridzEvent

data object GameEnded : GridzEvent

data object GameReset : GridzEvent

data object TileEntered : GridzEvent

class TileLit(val tile: GridzTile) : GridzEvent

class TileLitDeceased(val tile: GridzTile) : GridzEvent

class TeleportStarted(val from: GridzTile, val to: GridzTile) : GridzEvent

class ItemCollected(val item: GridzItem) : GridzEvent

class ItemConsumed(val item: GridzItem) : GridzEvent

class ExitEntered(val exit: GridzTile) : GridzEvent

data object ExitOpened : GridzEvent

data object TeleportEnded : GridzEvent
