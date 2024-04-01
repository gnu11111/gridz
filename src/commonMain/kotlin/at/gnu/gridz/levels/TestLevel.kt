package at.gnu.gridz.levels

import korlibs.io.lang.indexOfOrNull

open class TestLevel {

    open val layout = """
        ************ *******
        *                  *
        *            ****  *
        *             ***  *
        *  ****      ****  *
        *  *     o         *
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

    open val title = "Test"

    open val rows: Int get() = layout.size
    open val cols: Int get() = layout.maxOf { it.length }
    open val startX: Int get() = layout.firstOrNull { it.contains('o') }?.indexOfOrNull('o') ?: (cols / 2)
    open val startY: Int
        get() = if (layout.any { it.contains('o')}) layout.indexOfFirst { it.contains('o') } else rows / 2
}
