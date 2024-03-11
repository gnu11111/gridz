package at.gnu.gridz

interface GridzRenderer {

    suspend fun init(gridzGame: GridzGame)
    fun close()
}
