package at.gnu.gridz

sealed class GridzItem(open val x: Int, open val y: Int)

class Key(override val x: Int, override val y: Int) : GridzItem(x, y)
