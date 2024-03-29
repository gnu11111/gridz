package at.gnu.gridz.levels

class EmptyLevel : GridzLevel() {

    override val layout = """
        *                  *


















        *                  *
    """.trimIndent().split("\n")

    override val number = 3
    override val title = "NOTHING"
}
