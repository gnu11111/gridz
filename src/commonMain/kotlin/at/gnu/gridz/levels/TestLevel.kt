package at.gnu.gridz.levels

class TestLevel {

    val layout = """
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

    val rows = layout.size
    val cols = layout.maxOf { it.length }
    val startX = cols / 2
    val startY = rows / 2
}
