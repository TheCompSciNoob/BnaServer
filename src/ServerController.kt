fun main(args: Array<String>) {
    val app = require("express")()
    val server = require("http").Server(app)
    val io = require("socket.io")(server)
}

external fun require(module: String): dynamic

/*
import kotlin.js.Json

external class Server {
    fun listen(port: Int, callback: () -> Unit)
}

external class IO {
    fun on(event: String, callback: (Socket) -> Unit)
}

external class Socket {
    fun on(event: String, callback: (dynamic) -> Unit)

    fun emit(name: String, info: dynamic)

    var id: String

    var broadcast: Socket
}
*/
