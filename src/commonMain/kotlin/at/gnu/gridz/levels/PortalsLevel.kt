package at.gnu.gridz.levels

import at.gnu.gridz.Key
import at.gnu.gridz.Pill
import at.gnu.gridz.Teleport

class PortalsLevel : GridzLevel() {

    override val layout = """
        *3***********1*
        4k...........k2
        *.           .*
        *.           .*
        *.   *****   .*
        *.  ** 0 **  .*
        *.  * x x *  .*
        *.  * xox *  .*
        *.  * x x *  .*
        *.  ** 3 **  .*
        *.   *****   .*
        *.           .*
        *.           .*
        2k...........k4
        *1***********0*
    """.trimIndent().split("\n")

    override val title = "Portals"
    override val tailLitTime = 0L
    override val maxInventory = 4
    override val tasks = mapOf(Key.NAME to 4, Teleport.NAME to 4, Pill.NAME to 44)
}
