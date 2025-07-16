package com.ali.demono.features.game.data.repository

import com.ali.demono.features.game.domain.model.GameSession
import com.ali.demono.features.game.domain.model.GameType
import com.ali.demono.features.game.domain.model.Player
import com.ali.demono.features.game.domain.repository.GameRepository

class InMemoryGameRepository : GameRepository {
    private var session = GameSession()

    override suspend fun getGameSession(): GameSession = session

    override suspend fun addPlayer(player: Player) {
        session = session.copy(players = session.players + player)
    }

    override suspend fun updatePlayerScore(playerId: String, score: Int) {
        session = session.copy(players = session.players.map {
            if (it.id == playerId) it.copy(score = it.score + score) else it
        })
    }

    override suspend fun updatePlayerName(playerId: String, newName: String) {
        session = session.copy(players = session.players.map {
            if (it.id == playerId) it.copy(name = newName) else it
        })
    }

    override suspend fun deletePlayer(playerId: String) {
        session = session.copy(players = session.players.filter { it.id != playerId })
    }

    override suspend fun resetGame() {
        session = GameSession(gameType = session.gameType)
    }

    override suspend fun setGameType(type: GameType) {
        session = session.copy(gameType = type, players = emptyList(), round = 1)
    }

    override suspend fun nextRound() {
        println("DEBUG: InMemoryGameRepository nextRound - players before reset: ${session.players.map { "${it.name}: ${it.score}" }}")

        // Reset all player scores to 0 for the new round
        val resetPlayers = session.players.map { it.copy(score = 0) }
        session = session.copy(
            round = session.round + 1,
            players = resetPlayers
        )

        println("DEBUG: InMemoryGameRepository nextRound - players after reset: ${session.players.map { "${it.name}: ${it.score}" }}")
    }

    override suspend fun setPlayerScore(playerId: String, score: Int) {
        session = session.copy(players = session.players.map {
            if (it.id == playerId) it.copy(score = score) else it
        })
    }

    override suspend fun getCurrentPlayers(): List<Player> {
        return session.players
    }

    override suspend fun loadLastSession() {
        // For in-memory repository, nothing to load
    }
} 