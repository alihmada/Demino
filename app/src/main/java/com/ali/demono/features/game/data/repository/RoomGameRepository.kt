package com.ali.demono.features.game.data.repository

import com.ali.demono.features.game.data.database.GameDatabase
import com.ali.demono.features.game.data.model.GameSessionEntity
import com.ali.demono.features.game.data.model.PlayerEntity
import com.ali.demono.features.game.data.model.PlayerScoreEntity
import com.ali.demono.features.game.domain.model.GameSession
import com.ali.demono.features.game.domain.model.GameType
import com.ali.demono.features.game.domain.model.Player
import com.ali.demono.features.game.domain.repository.GameRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomGameRepository @Inject constructor(
    private val database: GameDatabase
) : GameRepository {

    private val playerDao = database.playerDao()
    private val gameSessionDao = database.gameSessionDao()
    private val playerScoreDao = database.playerScoreDao()

    override suspend fun getGameSession(): GameSession {
        val sessionEntity = gameSessionDao.getCurrentSession().first()
        return sessionEntity?.let { entity ->
            GameSession(
                players = emptyList(), // Will be populated separately
                gameType = GameType.valueOf(entity.gameType),
                round = entity.currentRound
            )
        } ?: GameSession()
    }

    override suspend fun addPlayer(player: Player) {

        // Get current session to determine the correct round number
        val currentSession = gameSessionDao.getCurrentSession().first()
        val currentRound = currentSession?.currentRound ?: 1

        val playerEntity = PlayerEntity(
            id = player.id,
            name = player.name,
            currentScore = player.score,
            roundNumber = currentRound
        )
        playerDao.insertPlayer(playerEntity)

        // Initialize game session if it doesn't exist
        if (currentSession == null) {
            gameSessionDao.insertSession(GameSessionEntity())
        }
    }

    override suspend fun updatePlayerScore(playerId: String, score: Int) {
        // For cumulative scoring, we add the score to the current score
        playerDao.addScoreToPlayer(playerId, score)

        // Also store the score for this round
        val currentSession = gameSessionDao.getCurrentSession().first()
        val roundNumber = currentSession?.currentRound ?: 1

        val playerScoreEntity = PlayerScoreEntity(
            playerId = playerId,
            roundNumber = roundNumber,
            score = score
        )
        playerScoreDao.insertPlayerScore(playerScoreEntity)
    }

    override suspend fun updatePlayerName(playerId: String, newName: String) {
        playerDao.updatePlayerName(playerId, newName)
    }

    override suspend fun deletePlayer(playerId: String) {
        playerDao.deletePlayerById(playerId)
    }

    override suspend fun resetGame() {
        val currentSession = gameSessionDao.getCurrentSession().first()
        val roundNumber = currentSession?.currentRound ?: 1
        val currentGameType = currentSession?.gameType ?: "MASRI"

        // Reset scores for current round
        playerDao.resetScoresForRound(roundNumber)

        // Delete all players
        playerDao.deleteAllPlayers()

        // Reset game session
        gameSessionDao.insertSession(GameSessionEntity())
        // Restore the previous game type
        gameSessionDao.updateGameType(currentGameType)
    }

    override suspend fun setGameType(type: GameType) {
        gameSessionDao.updateGameType(type.name)
    }

    override suspend fun nextRound() {
        val currentSession = gameSessionDao.getCurrentSession().first()
        val currentRound = currentSession?.currentRound ?: 1


        // Save current round scores to history
        val players = playerDao.getAllPlayers().first()

        players.forEach { playerEntity ->
            if (playerEntity.currentScore > 0) {
                val playerScoreEntity = PlayerScoreEntity(
                    playerId = playerEntity.id,
                    roundNumber = currentRound,
                    score = playerEntity.currentScore
                )
                playerScoreDao.insertPlayerScore(playerScoreEntity)
            }
        }

        // Increment round number
        gameSessionDao.incrementRound()

        // Reset all current player scores to 0
        playerDao.resetAllScores()

        // Update all players to the new round number
        val newRound = currentRound + 1
        playerDao.updateAllPlayersToRound(newRound)

    }

    // Additional methods for loading players
    override suspend fun getCurrentPlayers(): List<Player> {
        val currentSession = gameSessionDao.getCurrentSession().first()
        val roundNumber = currentSession?.currentRound ?: 1


        val players = playerDao.getPlayersByRound(roundNumber).first()

        return players.map { entity ->
            Player(
                id = entity.id,
                name = entity.name,
                score = entity.currentScore
            )
        }
    }

    override suspend fun loadLastSession() {
        val currentSession = gameSessionDao.getCurrentSession().first()
        if (currentSession == null) {
            // Initialize with default session
            gameSessionDao.insertSession(GameSessionEntity())
        }
    }

    override suspend fun setPlayerScore(playerId: String, score: Int) {
        playerDao.setPlayerScore(playerId, score)
    }
} 