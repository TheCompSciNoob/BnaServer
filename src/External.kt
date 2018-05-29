import kotlin.js.json

external class Server {
    fun listen(port: Int, callback: () -> Unit)
}

external class IO {
    fun on(event: String, callback: (Socket) -> Unit)
}

external class Socket {
    var id: String
    var broadcast: Socket

    fun on(event: String, callback: (dynamic) -> Unit)

    fun emit(name: String, vararg info: dynamic)

    fun join(roomName: String)

    fun leave(roomName: String)
}

external fun require(module: String): dynamic

