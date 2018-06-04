import model.GameModel
import kotlin.js.Console
import kotlin.js.JSON
import kotlin.js.console
import kotlin.js.json

class PlayerController(private val playerSocket: Socket, val gameController: GameController, val playerModel: GameModel) {

    init {
        playerSocket.configPlayer()
    }

    private fun Socket.configPlayer(): Unit = with(playerSocket) {
        on("UpdateSelfPosition") {
            console.log("MovePlayer")
            val newX = it.newX as Int
            val newY = it.newY as Int
            if (gameController.entitiesMap.any { it.second.x == newX && it.second.y == newY }) {
                respond(false)
                emit("PlayerGameStateUpdate", json("gameState" to gameController.gameState))
            } else {
                respond(true)
                playerModel.x = newX
                playerModel.y = newY
                gameController.dmSocket.emit("DMGameStateUpdate", json("gameState" to gameController.gameState))
                gameController.dmSocket.broadcast.emit("PlayerGameStateUpdate", json("gameState" to gameController.gameState))
                console.log("Player position updated")
            }
        }
        on("UpdateGameState") {
            console.log("UpdateGameState")
            val gameStateJSON = it.gameStateJSON as String
            gameController.gameState = JSON.parse(gameStateJSON)
            gameController.dmSocket.emit("DMGameStateUpdate", json("gameState" to gameController.gameState))
            gameController.dmSocket.broadcast.emit("PlayerGameStateUpdate", json("gameState" to gameController.gameState))
        }
        on("EndTurn") {
            console.log("EndTurn")
            gameController.nextTurn()
        }
    }

    private fun Socket.respond(changeApproved: Boolean) {
        //responds to socket whether the action is approved
        emit("Response", json("changeApproved" to changeApproved))
    }
}