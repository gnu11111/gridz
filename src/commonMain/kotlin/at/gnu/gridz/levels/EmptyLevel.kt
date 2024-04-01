package at.gnu.gridz.levels

class EmptyLevel : TestLevel() {

    override val layout = """
        *                  *


















        *                  *
    """.trimIndent().split("\n")

    override val title = "NOTHING"
}
