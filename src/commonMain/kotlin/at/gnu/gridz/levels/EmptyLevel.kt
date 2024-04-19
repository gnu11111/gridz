package at.gnu.gridz.levels

class EmptyLevel : GridzLevel() {

    override val layout = """
        2                  1









                  x








        1                  2
    """.trimIndent().split("\n")

    override val title = "NOTHING"
    override val maxInventory = 0
    override val tailLitTime = 5000L
    override val tasks = emptyMap<String, Int>()
}
