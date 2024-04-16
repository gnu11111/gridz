package at.gnu.gridz.levels

import at.gnu.gridz.Empty

class EmptyLevel : GridzLevel() {

    override val layout = """
        2                  1









                  x








        1                  2
    """.trimIndent().split("\n")

    override val title = "NOTHING"
    override val maxInventory = 0
    override val tailLitTime = 5000L
    override val tasks = mapOf(Empty.NAME to 10)
}
