package at.gnu.gridz.levels

class PacmanLevel : TestLevel() {

    override val layout = """
        *******************
        *        *        *
        * ** *** * *** ** *
        * ** *** * *** ** *
        *                 *
        * ** * ***** * ** *
        *    *   *   *    *
        **** *** * *** ****
           * *       * *
        **** * *xxx* * ****
               *   *     
        **** * ***** * ****
           * *       * *
        **** * ***** * ****
        *        *        *
        * ** *** * *** ** *
        *  *           *  *
        ** * * ***** * * **
        *    *   *   *    *
        * ****** * ****** *
        *                 *
        *******************
    """.trimIndent().split("\n")

    override val title = "Pacman!"
    override val startY = 16
}
