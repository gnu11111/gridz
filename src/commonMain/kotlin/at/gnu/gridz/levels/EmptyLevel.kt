package at.gnu.gridz.levels

import at.gnu.gridz.Empty
import at.gnu.gridz.Teleport

class EmptyLevel : TestLevel() {

    override val layout = """
        2                  1









                  x








        1                  2
    """.trimIndent().split("\n")

    override val title = "NOTHING"
    override val maxInventory = 0
    override val requirements = mapOf(Teleport.NAME to 2, Empty.NAME to 50)
}
