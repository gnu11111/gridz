
import at.gnu.gridz.*
import at.gnu.gridz.korge.KorgeRenderer

suspend fun main() {
    KorgeRenderer().apply { init(GridzGame()) }
}
