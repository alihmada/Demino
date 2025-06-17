package com.ali.demono.features.game.domain.repository

import com.ali.demono.features.game.domain.model.GameSession
import com.ali.demono.features.game.domain.model.Player

interface GameRepository {
    suspend fun getGameSession(): GameSession
    suspend fun addPlayer(player: Player)
    suspend fun updatePlayerScore(playerId: String, score: Int)
    suspend fun resetGame()
    suspend fun setGameType(type: com.ali.demono.features.game.domain.model.GameType)
    suspend fun nextRound()
} 