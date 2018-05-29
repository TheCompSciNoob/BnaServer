import model.GameModel
import kotlin.js.json

class PlayerController(val playerSocket: Socket, val gameController: GameController, val playerModel: GameModel) {

    init {
        playerSocket.configPlayer()
    }

    private fun Socket.configPlayer(): Unit = with(playerSocket) {
        on("MovePlayer") {
            //TODO: check if player present, if not can move
            //TODO: refresh all players when action completed
            if (gameController.entityInTurn === playerModel) {
                respond(true)
                playerModel.x = it.newX as Int
                playerModel.y = it.newY as Int
                broadcast.emit("UpdateGameState", gameController.gameState)
            } else respond(false)
        }
        on("UpdateGameState") {
            if (gameController.entityInTurn === playerModel) {
                respond(true)
                val gameStateJSON = it.gameStateJSON as String
                gameController.gameState = JSON.parse(gameStateJSON)
                broadcast.emit("UpdateGameState", gameStateJSON)
            } else respond(false)
        }
        on("EndTurn") {
            if (gameController.entityInTurn === playerModel) {
                respond(true)
                gameController.nextTurn()
            } else respond(false)
        }
    }

    private fun Socket.respond(changeApproved: Boolean) {
        //responds to socket whether the action is approved
        emit("response", json("changeApproved" to changeApproved))
    }
}