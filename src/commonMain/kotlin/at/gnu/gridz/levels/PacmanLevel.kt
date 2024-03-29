package at.gnu.gridz.levels

class PacmanLevel : GridzLevel() {

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

    override val number = 2
    override val title = "Pacman!"
    override val startY = 16
}
