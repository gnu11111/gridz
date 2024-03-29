package at.gnu.gridz.korge

import at.gnu.gridz.GridzGame
import at.gnu.gridz.GridzGame.Companion.HEIGHT
import at.gnu.gridz.GridzGame.Companion.WIDTH
import korlibs.event.Key
import korlibs.image.color.Colors
import korlibs.korge.Korge
import korlibs.korge.scene.*
import korlibs.korge.view.*
import korlibs.math.geom.Size

class KorgeRenderer {

    suspend fun init(gridzGame: GridzGame) {
        val scoreWidth = WIDTH / 3
        val assets = KorgeAssets().apply { load() }
        Korge(
            title = GridzGame.NAME,
            windowSize = Size(WIDTH + scoreWidth, HEIGHT),
            virtualSize = Size(WIDTH + scoreWidth, HEIGHT),
            backgroundColor = Colors[BACKGROUND_COLOR]
        ) {
            sceneContainer().changeTo { GameScene(gridzGame, assets, scoreWidth) }
            addUpdater {
                if (keys[Key.ESCAPE]) views.gameWindow.close(0)
            }
        }
    }

    companion object {
        const val BACKGROUND_COLOR = "#2b1020"
    }
}