fun main(args: Array<String>) {
    val app = require("express")()
    val server = require("http").Server(app)
    val io = require("socket.io")(server)
}

external fun require(module: String): dynamic