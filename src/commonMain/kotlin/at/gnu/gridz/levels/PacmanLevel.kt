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
        **** * ***** * ****
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
