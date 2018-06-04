import model.GameModel
import model.GameState
import kotlin.js.JSON
import kotlin.js.console
import kotlin.js.json

class GameController(val dmSocket: Socket, gameStateJSON: String) {
    //Server variables
    var roomName = "Room ${dmSocket.id}"
    private var numPlayersJoined = -1
    //parse GameState from a JSON String
    var gameState = JSON.parse<GameState>(gameStateJSON)
    //keeping track of turns
    val entitiesMap = mutableListOf<Pair<String?, GameModel>>()
    var turnPosition = -1

    init {
        dmSocket.configDM()
    }

    fun joinPlayer(playerSocket: Socket) {
        numPlayersJoined++
        if (numPlayersJoined >= gameState.players.size) {
            playerSocket.emit("RoomFull")
            playerSocket.leave(roomName)
        } else {
            playerSocket.broadcast.emit("PlayerConnectedChange", json("id" to playerSocket.id))
            entitiesMap.add(playerSocket.id to gameState.players[numPlayersJoined])
            PlayerController(playerSocket, this, gameState.players[numPlayersJoined])
        }
    }

    fun startGame() {
        val enemiesMap: List<Pair<String?, GameModel>> = gameState.enemies.map { null to it }
        entitiesMap.addAll(enemiesMap)
        entitiesMap.shuffle()
        for ((index, info) in entitiesMap.withIndex()) {
            info.second.initiative = entitiesMap.size - index
        }
        dmSocket.broadcast.emit("StartGame", json("gameState" to gameState))
    }

    private fun Socket.configDM() {
        on("disconnect") {
            console.log("disconnect")
            dmSocket.broadcast.apply {
                leave(roomName)
                emit("DmDisconnected", json("roomName" to roomName))
            }
        }
        on("UpdateGameStateAsDM") {
            console.log("UpdateGameState")
            val gameStateJSON = it.gameStateJSON as String
            gameState = JSON.parse(gameStateJSON)
            broadcast.emit("UpdateGameState", gameState)
        }
        on("StartGame") {
            startGame()
        }
        on("NextTurn") {
            nextTurn()
        }
    }

    fun nextTurn() {
        if (entitiesMap.any { it.second.health > 0 }) {
            do {
                turnPosition = (turnPosition + 1) % entitiesMap.size
            } while (entitiesMap[turnPosition].second.health <= 0)
            val entityInTurn = entitiesMap[turnPosition]
            if (entityInTurn.first == null) {
                dmSocket.emit("EnemyTurnChange", json("gameState" to gameState))
                dmSocket.broadcast.emit("TurnChangeOther", json("gameState" to gameState, "x" to entityInTurn.second.x, "y" to entityInTurn.second.y))
            } else {
                dmSocket.emit("PlayerTurnChange", json("gameState" to gameState, "x" to entityInTurn.second.x, "y" to entityInTurn.second.y))
                val playerSocket = dmSocket.broadcast.to(id = entityInTurn.first!!)
                playerSocket.emit("TurnChangeSelf", json("gameState" to gameState))
                playerSocket.broadcast.emit("TurnChangeOther", json("gameState" to gameState, "x" to entityInTurn.second.x, "y" to entityInTurn.second.y))
            }
        } else {
            dmSocket.emit("GameEnd")
            dmSocket.broadcast.emit("GameEnd")
        }
    }
}