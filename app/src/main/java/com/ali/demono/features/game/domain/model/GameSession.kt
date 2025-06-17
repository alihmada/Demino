package com.ali.demono.features.game.domain.model

// Represents a game session with players and game type

data class GameSession(
    val players: List<Player> = emptyList(),
    val gameType: GameType = GameType.MASRI,
    val round: Int = 1
) 