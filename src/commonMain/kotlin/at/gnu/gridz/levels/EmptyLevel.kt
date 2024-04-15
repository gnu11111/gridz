package at.gnu.gridz.levels

class EmptyLevel : TestLevel() {

    override val layout = """
        2                  1









                  x








        1                  2
    """.trimIndent().split("\n")

    override val title = "NOTHING"
}
