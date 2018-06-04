import model.GameModel
import kotlin.js.Console
import kotlin.js.json

class PlayerController(val playerSocket: Socket, val gameController: GameController, val playerModel: GameModel) {

    init {
        playerSocket.configPlayer()
    }

    private fun Socket.configPlayer(): Unit = with(playerSocket) {
        on("MovePlayer") {
            console.log("MovePlayer")
            if (gameController.entityInTurn === playerModel) {
                val newX: Int = it.newX
                val newY: Int = it.newY
                if (gameController.gameState.players.any { it.x == newX && it.y == newY } ||
                        gameController.gameState.players.any { it.x == newX && it.y == newY }) {
                    respond(false)
                } else {
                    respond(true)
                    playerModel.x = it.newX as Int
                    playerModel.y = it.newY as Int
                    broadcast.emit("UpdateGameState", gameController.gameState)
                    console.log("Player position updated")
                }
            } else {
                respond(false)
                emit("UpdateGameState", gameController.gameState)
            }
        }
        on("UpdateGameState") {
            console.log("UpdateGameState")
            if (gameController.entityInTurn === playerModel) {
                respond(true)
                val gameStateJSON = it.gameStateJSON as String
                gameController.gameState = JSON.parse(gameStateJSON)
                broadcast.emit("UpdateGameState", gameStateJSON)
            } else {
                respond(false)
                emit("UpdateGameState", gameController.gameState)
            }
        }
        on("EndTurn") {
            console.log("EndTurn")
            if (gameController.entityInTurn === playerModel) {
                respond(true)
                gameController.nextTurn()
            } else {
                respond(false)
                emit("UpdateGameState", gameController.gameState)
            }
        }
    }

    private fun Socket.respond(changeApproved: Boolean) {
        //responds to socket whether the action is approved
        emit("response", json("changeApproved" to changeApproved))
    }
}