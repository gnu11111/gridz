package at.gnu.gridz.korge

import at.gnu.gridz.GridzGame
import korlibs.event.Key
import korlibs.image.color.Colors
import korlibs.korge.Korge
import korlibs.korge.scene.*
import korlibs.korge.view.*
import korlibs.math.geom.Size

class KorgeRenderer {

    suspend fun init(gridzGame: GridzGame) {
        val scoreWidth = WIDTH / 3
        KorgeAssets.load()
        Korge(
            title = GridzGame.NAME,
            windowSize = Size(WIDTH + scoreWidth, HEIGHT),
            virtualSize = Size(WIDTH + scoreWidth, HEIGHT),
            backgroundColor = Colors[BACKGROUND_COLOR]
        ) {
            sceneContainer().changeTo { KorgeScene(gridzGame, scoreWidth) }
            addUpdater {
                if (keys[Key.ESCAPE]) views.gameWindow.close(0)
            }
        }
    }

    companion object {
        const val BACKGROUND_COLOR = "#1a1a1a"
        const val WIDTH = 480
        const val HEIGHT = 480
    }
}
