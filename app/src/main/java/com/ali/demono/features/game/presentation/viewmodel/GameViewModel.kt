package com.ali.demono.features.game.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ali.demono.features.game.domain.model.Player
import com.ali.demono.features.game.domain.repository.GameRepository
import com.ali.demono.features.game.domain.usecase.AddPlayerUseCase
import com.ali.demono.features.game.domain.usecase.NextRoundUseCase
import com.ali.demono.features.game.domain.usecase.ResetGameUseCase
import com.ali.demono.features.game.domain.usecase.SetGameTypeUseCase
import com.ali.demono.features.game.domain.usecase.UpdateScoreUseCase
import com.ali.demono.features.game.presentation.state.GameUiEvent
import com.ali.demono.features.game.presentation.state.GameUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class GameViewModel @Inject constructor(
    private val addPlayerUseCase: AddPlayerUseCase,
    private val updateScoreUseCase: UpdateScoreUseCase,
    private val resetGameUseCase: ResetGameUseCase,
    private val setGameTypeUseCase: SetGameTypeUseCase,
    private val nextRoundUseCase: NextRoundUseCase,
    private val repository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    fun handleEvent(event: GameUiEvent) {
        Log.d("RX", "handleEvent: $event")
        when (event) {
            is GameUiEvent.AddPlayer -> addPlayer(event.name)
            is GameUiEvent.UpdatePlayerScore -> updatePlayerScore(event.playerId, event.score)
            is GameUiEvent.EditPlayer -> editPlayer(event.playerId, event.newName, event.newScore)
            is GameUiEvent.DeletePlayer -> deletePlayer(event.playerId)
            is GameUiEvent.SetGameType -> setGameType(event.gameType)
            is GameUiEvent.ConfirmGameTypeChange -> confirmGameTypeChange(event.gameType)
            is GameUiEvent.ResetGame -> resetGame()
            is GameUiEvent.NextRound -> nextRound()
            is GameUiEvent.ConfirmNextRound -> confirmNextRound()
            is GameUiEvent.CancelNextRound -> cancelNextRound()
            is GameUiEvent.ClearPlayerLimitMessage -> clearPlayerLimitMessage()
            is GameUiEvent.CancelGameTypeChange -> cancelGameTypeChange()
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                loadLastSession()
                updateGameSession()
                loadCurrentPlayers()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Unknown error occurred"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private suspend fun loadLastSession() {
        repository.loadLastSession()
    }

    private suspend fun updateGameSession() {
        val session = repository.getGameSession()
        _uiState.value = _uiState.value.copy(gameSession = session)
    }

    private suspend fun loadCurrentPlayers() {
        val players = repository.getCurrentPlayers()
        _uiState.value = _uiState.value.copy(players = players)
    }

    private fun addPlayer(name: String) {
        Log.d("RX", "addPlayer: $name")
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val currentPlayers = currentState.players
                val currentGameType = currentState.currentGameType

                if (!currentGameType.canAddPlayer(currentPlayers.size)) {
                    _uiState.value = currentState.copy(
                        showPlayerLimitMessage = currentGameType
                    )
                    return@launch
                }

                addPlayerUseCase(Player(name = name))
                Log.d("RX", "addPlayerUseCase called")

                loadCurrentPlayers()
                updateGameSession()
                Log.d("RX", "loadCurrentPlayers and updateGameSession called")

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to add player"
                )
            }
        }
    }

    private fun updatePlayerScore(playerId: String, score: Int) {
        viewModelScope.launch {
            try {
                updateScoreUseCase(playerId, score)
                loadCurrentPlayers()
                updateGameSession()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update score"
                )
            }
        }
    }

    private fun editPlayer(playerId: String, newName: String, newScore: Int) {
        viewModelScope.launch {
            try {
                // Update player name
                repository.updatePlayerName(playerId, newName)

                // Update player score
                repository.setPlayerScore(playerId, newScore)

                loadCurrentPlayers()
                updateGameSession()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to edit player"
                )
            }
        }
    }

    private fun deletePlayer(playerId: String) {
        viewModelScope.launch {
            try {
                repository.deletePlayer(playerId)
                loadCurrentPlayers()
                updateGameSession()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete player"
                )
            }
        }
    }

    private fun setGameType(gameType: com.ali.demono.features.game.domain.model.GameType) {
        Log.d("RX", "setGameType: $gameType")
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    showGameTypeChangeDialog = gameType
                )
                Log.d("RX", "showGameTypeChangeDialog set to $gameType")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to set game type"
                )
            }
        }
    }

    private fun confirmGameTypeChange(gameType: com.ali.demono.features.game.domain.model.GameType) {
        Log.d("RX", "confirmGameTypeChange: $gameType")
        viewModelScope.launch {
            try {
                changeGameType(gameType)
                _uiState.value = _uiState.value.copy(showGameTypeChangeDialog = null)
                Log.d("RX", "showGameTypeChangeDialog cleared")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to change game type"
                )
            }
        }
    }

    private fun cancelGameTypeChange() {
        _uiState.value = _uiState.value.copy(showGameTypeChangeDialog = null)
        Log.d("RX", "showGameTypeChangeDialog cleared")
    }

    private suspend fun changeGameType(gameType: com.ali.demono.features.game.domain.model.GameType) {
        Log.d("RX", "changeGameType: $gameType")
        setGameTypeUseCase(gameType)
        updateGameSession()
        Log.d("RX", "changeGameType completed")
    }

    private fun resetGame() {
        viewModelScope.launch {
            try {
                resetGameUseCase()
                _uiState.value = _uiState.value.copy(players = emptyList())
                updateGameSession()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to reset game"
                )
            }
        }
    }

    private fun nextRound() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState.hasScores) {
                    _uiState.value = currentState.copy(showNextRoundConfirmation = true)
                } else {
                    proceedToNextRound()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to proceed to next round"
                )
            }
        }
    }

    private fun confirmNextRound() {
        viewModelScope.launch {
            try {
                proceedToNextRound()
                _uiState.value = _uiState.value.copy(showNextRoundConfirmation = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to confirm next round"
                )
            }
        }
    }

    private fun cancelNextRound() {
        _uiState.value = _uiState.value.copy(showNextRoundConfirmation = false)
    }

    private suspend fun proceedToNextRound() {
        nextRoundUseCase()
        loadCurrentPlayers()
        updateGameSession()
    }

    private fun clearPlayerLimitMessage() {
        _uiState.value = _uiState.value.copy(showPlayerLimitMessage = null)
    }

    fun restorePlayer(player: Player) {
        viewModelScope.launch {
            try {

                // Check if player already exists to avoid conflicts
                val currentPlayers = repository.getCurrentPlayers()
                val existingPlayer = currentPlayers.find { it.name == player.name }

                if (existingPlayer != null) {
                    repository.setPlayerScore(existingPlayer.id, player.score)
                } else {
                    repository.addPlayer(player)
                }


                // Force reload players to ensure UI updates
                loadCurrentPlayers()

                updateGameSession()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to restore player"
                )
            }
        }
    }
} 