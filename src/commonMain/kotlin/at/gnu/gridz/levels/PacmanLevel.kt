package at.gnu.gridz.levels

import at.gnu.gridz.Key
import at.gnu.gridz.Pill

class PacmanLevel : TestLevel() {

    override val layout = """
        *******************
        *........*........*
        *.**.***.*.***.**.*
        *k**.***.*.***.**k*
        *.................*
        *.**.* ***** *.**.*
        *....*   *   *....*
        ****.*** * ***.****
           *.*       *.*
        ****.* *xxx* *.****
            .  *   *  .  
        ****.* ***** *.****
           *.*       *.*
        ****.* ***** *.****
        *........*........*
        *.**.***.*.***.**.*
        *k.*..... .....*.k*
        **.*.*.*****.*.*.**
        *....*...*...*....*
        *.******.*.******.*
        *.................*
        *******************
    """.trimIndent().split("\n")

    override val title = "Pacman!"
    override val startY = 16
    override val tailLitTime = 0L
    override val maxInventory = 4
    override val requirements = mapOf(Key.NAME to 4, Pill.NAME to 144)
}
