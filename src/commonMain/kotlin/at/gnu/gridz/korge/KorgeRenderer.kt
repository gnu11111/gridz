package at.gnu.gridz.korge

import at.gnu.gridz.GridzGame
import korlibs.image.color.Colors
import korlibs.korge.Korge
import korlibs.korge.scene.*
import korlibs.korge.service.storage.storage
import korlibs.math.geom.Size

class KorgeRenderer {

    suspend fun init(gridzGame: GridzGame) {
        KorgeAssets.load()
        Korge(
            title = GridzGame.NAME,
            windowSize = Size(WIDTH + INFO_WIDTH, HEIGHT),
            virtualSize = Size(WIDTH + INFO_WIDTH, HEIGHT),
            backgroundColor = Colors[BACKGROUND_COLOR]
        ) {
            sceneContainer().changeTo { KorgeScene(gridzGame, this@Korge.views.storage, INFO_WIDTH) }
        }
    }

    companion object {
        const val BACKGROUND_COLOR = "#1a1a1a"
        const val WIDTH = 480
        const val HEIGHT = 480
        const val INFO_WIDTH = 160
    }
}
