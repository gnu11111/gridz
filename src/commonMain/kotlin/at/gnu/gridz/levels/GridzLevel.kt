package at.gnu.gridz.levels

import at.gnu.gridz.Empty
import at.gnu.gridz.Key
import at.gnu.gridz.Pill
import at.gnu.gridz.Teleport
import korlibs.io.lang.indexOfOrNull

open class GridzLevel {

    open val layout = """
        ************ *******
        *                  *
        *            ****  *
        *        ....kxx*  *
        *  **** .    ****  *
        *  *1... o         *
        *  ****  .   ****  *
        *        .         *
                 .          
        *********. *********
        *********. *********
        *    .....         *
        * * *.****  ****** *
        *  k.......*...*k* *
        * * * * * .*1*...* *
        *       * .***.* * *
        * * * * * .....*   *
        * *   * ********** *
        *                  *
        ********************
    """.trimIndent().split("\n")

    open val title = "Test"
    open val maxInventory = 10
    open val tailLitTime = 60000L
    open val tasks = mapOf(Key.NAME to 3, Pill.NAME to 40, Teleport.NAME to 1, Empty.NAME to 45)

    open val rows: Int get() = layout.size
    open val cols: Int get() = layout.maxOf { it.length }
    open val startX: Int
        get() = layout.firstOrNull { it.contains('o') }?.indexOfOrNull('o') ?: (cols / 2)
    open val startY: Int
        get() = if (layout.any { it.contains('o')}) layout.indexOfFirst { it.contains('o') } else rows / 2
}
