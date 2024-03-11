
import at.gnu.gridz.*

suspend fun main() {
    val gridzGame = GridzGame()
    val renderer = KorgeRenderer().apply { init(gridzGame) }
//        renderer.close()
}
