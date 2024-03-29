package at.gnu.gridz.levels

open class GridzLevel {

    open val layout = """
        ************ *******
        *                  *
        *            ****  *
        *             ***  *
        *  ****      ****  *
        *  *               *
        *  ****      ****  *
        *                  *
                            
        *********  *********
        *********  *********
        *                  *
        * * * ****  ****** *
        *          *   * * *
        * * * * *  * *   * *
        *       *  *** * * *
        * * * * *      *   *
        * *   * ********** *
        *                  *
        ************ *******
    """.trimIndent().split("\n")

    open val number = 1
    open val title = "Test"

    open val rows: Int get() = layout.size
    open val cols: Int get() = layout.maxOf { it.length }
    open val startX: Int get() = cols / 2
    open val startY: Int get() = rows / 2
}
