package at.gnu.gridz

sealed class GridzItem(open val x: Int, open val y: Int, open val collectable: Boolean)

class Key(override val x: Int, override val y: Int) : GridzItem(x, y, true)

class Pill(override val x: Int, override val y: Int) : GridzItem(x, y, false)
