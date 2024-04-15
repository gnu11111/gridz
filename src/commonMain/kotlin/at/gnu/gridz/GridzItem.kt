package at.gnu.gridz

sealed class GridzItem(open val name: String, open val x: Int, open val y: Int, open val collectable: Boolean)

class Key(override val x: Int, override val y: Int) : GridzItem(NAME, x, y, true) {
    companion object {
        const val NAME = "Key"
    }
}

class Pill(override val x: Int, override val y: Int) : GridzItem(NAME, x, y, false) {
    companion object {
        const val NAME = "Pill"
    }
}
