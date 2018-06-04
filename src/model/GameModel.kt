package model

data class GameModel(var x: Int, var y: Int, var health: Int, var initiative: Int) {
    constructor(): this(0, 0, 0, 0)
}