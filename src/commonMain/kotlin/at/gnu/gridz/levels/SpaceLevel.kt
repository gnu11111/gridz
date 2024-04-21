package at.gnu.gridz.levels

import at.gnu.gridz.Empty
import at.gnu.gridz.Pill

class SpaceLevel : GridzLevel() {

    override val layout = """
        *                                              *

            .

                                        .
                        .
                                                   .

                           .


              .             .
                                        .

           .
                                                     .


                 .                        .

                             .
                                               .
             .

                                x

               .                          .



                   .                .

                                                    .
                          .
        .                                 .


                      .                   .

                               .
             .               

                         .                            .

                 .              .             .

                      
        *                                              *
    """.trimIndent().split("\n")

    override val title = ".SPACE."
    override val maxInventory = 1
    override val tailLitTime = 50000L
    override val tasks = mapOf(Pill.NAME to 10, Empty.NAME to 99)
}
