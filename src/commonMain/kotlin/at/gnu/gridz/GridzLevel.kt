package at.gnu.gridz

class GridzLevel {

    val layout = """
        ********************
        *                  *
        *            ****  *
        *             * *  *
        *  ****      ****  *
        *  *  *            *
        *  ****      ****  *
        *                  *
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
        ********************
    """.trimIndent().split("\n")

    val rows = layout.size
    val cols = layout.maxOf { it.length }
}
