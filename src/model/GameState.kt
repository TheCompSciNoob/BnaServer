package model

data class GameState(var players: Array<GameModel>, var enemies: Array<GameModel>) {
    constructor(): this(emptyArray(), emptyArray())
}
