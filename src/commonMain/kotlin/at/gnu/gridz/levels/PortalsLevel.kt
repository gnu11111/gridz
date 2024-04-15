package at.gnu.gridz.levels

class PortalsLevel : TestLevel() {

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

    override val title = "Portals"
    override val tailLitTime = 0L
}
