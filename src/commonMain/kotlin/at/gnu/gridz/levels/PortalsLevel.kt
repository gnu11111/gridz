package at.gnu.gridz.levels

class PortalsLevel : GridzLevel() {

    override val layout = """
        *3****************1*
        4k                k2
        *                  *
        *                  *
        *                  *
        *                  *
        *                  *
        *       *****      *
        *      ** 0 **     *
        *      * xox *     *
        *      ** 3 **     *
        *       *****      *
        *                  *
        *                  *
        *                  *
        *                  *
        *                  *
        *                  *
        2k                k4
        *1****************0*
    """.trimIndent().split("\n")

    override val number = 4
    override val title = "Portals"
}
