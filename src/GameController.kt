import model.GameModel
import model.GameState
import kotlin.js.json

class GameController(val dmSocket: Socket, gameStateJSON: String) {
    //Server variables
    val roomName = "Room ${dmSocket.id}"
    private var numPlayersJoined = -1
    //parse GameState from a JSON String
    var gameState = JSON.parse<GameState>(gameStateJSON)
    //keeping track of turns
    private val combinedList = mutableListOf(*gameState.players, *gameState.enemies)
    var turnPosition = 0
    val entityInTurn: GameModel get() = combinedList[turnPosition]

    init {
        dmSocket.configDM()
        //random turns
        combinedList.shuffle()
        for((index, model) in combinedList.withIndex()) {
            model.initiative = combinedList.size - index
        }
    }

    fun joinPlayer(playerSocket: Socket) {
        numPlayersJoined++
        if (numPlayersJoined >= gameState.players.size) {
            playerSocket.emit("RoomFull")
            playerSocket.leave(roomName)
        } else {
            PlayerController(playerSocket, this, gameState.players[numPlayersJoined])
        }
    }

    private fun Socket.configDM() {
        on("disconnect") {
            dmSocket.broadcast.apply {
                leave(roomName)
                emit("DmDisconnected", json("roomName" to roomName))
            }
        }
        on("UpdateGameState") {
            val gameStateJSON = it.gameStateJSON as String
            gameState = JSON.parse(gameStateJSON)
            broadcast.emit("UpdateGameState", gameState)
        }
        on("StartGame") {
            broadcast.emit("StartGame")
        }
    }

    fun nextTurn() {
        if (combinedList.filter { it.health > 0 }.isEmpty()) {
            dmSocket.emit("GameEnd")
            dmSocket.broadcast.emit("GameEnd")
        }
        else {
            do {
                turnPosition = (turnPosition + 1) % combinedList.size
            } while (entityInTurn.health <= 0)
            dmSocket.emit("NextTurn", entityInTurn, gameState)
            dmSocket.broadcast.emit("NextTurn", entityInTurn, gameState)
        }
    }
}