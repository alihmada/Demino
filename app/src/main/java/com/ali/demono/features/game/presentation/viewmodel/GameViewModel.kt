package com.ali.demono.features.game.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ali.demono.features.game.data.repository.InMemoryGameRepository
import com.ali.demono.features.game.data.repository.RoomGameRepository
import com.ali.demono.features.game.domain.model.GameSession
import com.ali.demono.features.game.domain.model.GameType
import com.ali.demono.features.game.domain.model.Player
import com.ali.demono.features.game.domain.repository.GameRepository
import com.ali.demono.features.game.domain.usecase.AddPlayerUseCase
import com.ali.demono.features.game.domain.usecase.NextRoundUseCase
import com.ali.demono.features.game.domain.usecase.ResetGameUseCase
import com.ali.demono.features.game.domain.usecase.SetGameTypeUseCase
import com.ali.demono.features.game.domain.usecase.UpdateScoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val addPlayerUseCase: AddPlayerUseCase,
    private val updateScoreUseCase: UpdateScoreUseCase,
    private val resetGameUseCase: ResetGameUseCase,
    private val setGameTypeUseCase: SetGameTypeUseCase,
    private val nextRoundUseCase: NextRoundUseCase,
    private val repository: GameRepository
) : ViewModel() {

    private val _gameSession = MutableStateFlow(GameSession())
    val gameSession: StateFlow<GameSession> = _gameSession

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players

    // UI State for showing messages
    private val _showPlayerLimitMessage = MutableStateFlow<String?>(null)
    val showPlayerLimitMessage: StateFlow<String?> = _showPlayerLimitMessage

    private val _showGameTypeChangeDialog = MutableStateFlow<GameType?>(null)
    val showGameTypeChangeDialog: StateFlow<GameType?> = _showGameTypeChangeDialog

    private val _showNextRoundConfirmation = MutableStateFlow<Boolean>(false)
    val showNextRoundConfirmation: StateFlow<Boolean> = _showNextRoundConfirmation

    init {
        loadLastSession()
        updateGameSession()
        loadCurrentPlayers()
    }

    private fun loadLastSession() {
        viewModelScope.launch {
            when (repository) {
                is RoomGameRepository -> repository.loadLastSession()
                is InMemoryGameRepository -> repository.loadLastSession()
                else -> {
                    // For any other repository implementation, do nothing
                }
            }
            updateGameSession()
        }
    }

    private fun updateGameSession() {
        viewModelScope.launch {
            val session = repository.getGameSession()
            _gameSession.value = session
        }
    }

    fun addPlayer(name: String) {
        viewModelScope.launch {
            val currentPlayers = _players.value
            val currentGameType = _gameSession.value.gameType

            if (!currentGameType.canAddPlayer(currentPlayers.size)) {
                _showPlayerLimitMessage.value = currentGameType.getPlayerLimitMessage()
                return@launch
            }

            addPlayerUseCase(Player(name = name))
            loadCurrentPlayers()
            updateGameSession()
        }
    }

    fun updateScore(playerId: String, score: Int) {
        viewModelScope.launch {
            updateScoreUseCase(playerId, score)
            loadCurrentPlayers()
            updateGameSession()
        }
    }

    fun resetGame() {
        viewModelScope.launch {
            resetGameUseCase()
            _players.value = emptyList()
            updateGameSession()
        }
    }

    fun setGameType(type: GameType) {
        viewModelScope.launch {
            val currentPlayers = _players.value
            val hasGameInProgress = currentPlayers.isNotEmpty() ||
                    currentPlayers.any { it.score > 0 }

            if (hasGameInProgress) {
                // Show confirmation dialog
                _showGameTypeChangeDialog.value = type
            } else {
                // No game in progress, change directly
                changeGameType(type)
            }
        }
    }

    fun confirmGameTypeChange() {
        viewModelScope.launch {
            val newGameType = _showGameTypeChangeDialog.value
            if (newGameType != null) {
                changeGameType(newGameType)
                _showGameTypeChangeDialog.value = null
            }
        }
    }

    fun cancelGameTypeChange() {
        _showGameTypeChangeDialog.value = null
    }

    private suspend fun changeGameType(type: GameType) {
        setGameTypeUseCase(type)
        updateGameSession()
    }

    fun nextRound() {
        viewModelScope.launch {
            val currentPlayers = _players.value
            val hasScores = currentPlayers.any { it.score > 0 }
            
            if (hasScores) {
                _showNextRoundConfirmation.value = true
            } else {
                proceedToNextRound()
            }
        }
    }

    fun confirmNextRound() {
        viewModelScope.launch {
            proceedToNextRound()
            _showNextRoundConfirmation.value = false
        }
    }

    fun cancelNextRound() {
        _showNextRoundConfirmation.value = false
    }

    private suspend fun proceedToNextRound() {
        nextRoundUseCase()
        loadCurrentPlayers()
        updateGameSession()
    }

    fun clearPlayerLimitMessage() {
        _showPlayerLimitMessage.value = null
    }

    private fun loadCurrentPlayers() {
        viewModelScope.launch {
            val currentPlayers = when (repository) {
                is RoomGameRepository -> repository.getCurrentPlayers()
                is InMemoryGameRepository -> repository.getCurrentPlayers()
                else -> emptyList<Player>()
            }
            println("DEBUG: Loaded players with scores: ${currentPlayers.map { "${it.name}: ${it.score}" }}")
            _players.value = currentPlayers
        }
    }
} 