import model.GameModel
import model.GameState
import kotlin.js.json

val gameControllers = mutableListOf<GameController>()

fun main(args: Array<String>) {
    //modules
    val app = require("express")()
    val server: Server = require("http").Server(app)
    val io: IO = require("socket.io")(server)

    //handles joining players
    server.listen(8080) {
        console.log("Server is now running...")
    }
    io.on("connection", ::onPlayerJoined)
}

fun onPlayerJoined(socket: Socket): Unit = with(socket) {
    console.log("Player Connected! ID: $id")
    emit("SocketID", json("id" to id))
    broadcast.emit("NewPlayerJoined", json("id" to id))
    on("disconnect") {
        console.log("Player Disconnected: ID: $id")
        broadcast.emit("PlayerDisconnected", json("id" to id))
        if (gameControllers.removeAll { it.dmSocket.id == id }) {
            refreshRooms() //refresh rooms if the player is DM
        }
    }
    on("JoinAsDM") {
        //makes new room and joins the DM
        console.log("JoinAsDM")
        val roomName = "Room $id"
        join(roomName)
        refreshRooms()
        //gets GameState from the DM in form of String
//        val jsonString = it.gameState as String

        val players = arrayOf(GameModel(1, 1, 1, 1), GameModel(2, 2, 2, 2))
        val enemies = arrayOf(GameModel(3, 3, 3, 3), GameModel(4, 4, 4, 4))
        val gs = GameState(players, enemies)
        val jsonString = JSON.stringify(gs)

        console.log(gs)

        gameControllers.add(GameController(this, jsonString))
        emit("JoinedAsDM")
    }
    on("JoinAsPlayer") {
        console.log("JoinAsPlayer")
        val roomName = it.roomName as String
//TODO        val controller = gameControllers.find { it.roomName == roomName }
//TODO        controller?.joinPlayer(this) ?: emit("Join room failed", json("roomName" to roomName))
        emit("JoinedAsPlayer")
    }
    on("RefreshRooms") {
        console.log("RefreshRooms")
        refreshRooms()
    }
    //testing
    on("TestEmit") {
        val message = it.message //testing remove later
        console.log("$id: $message")
    }
}

private fun Socket.refreshRooms() = broadcast.emit("RefreshRooms", json("rooms" to gameControllers.map { it.roomName }.toTypedArray()))
