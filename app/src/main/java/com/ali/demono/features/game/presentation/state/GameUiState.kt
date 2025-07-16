package com.ali.demono.features.game.presentation.state

import com.ali.demono.features.game.domain.model.GameSession
import com.ali.demono.features.game.domain.model.GameType
import com.ali.demono.features.game.domain.model.Player

/**
 * Represents the UI state for the game screen
 */
data class GameUiState(
    val players: List<Player> = emptyList(),
    val gameSession: GameSession = GameSession(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showPlayerLimitMessage: GameType? = null,
    val showNextRoundConfirmation: Boolean = false,
    val showGameTypeChangeDialog: GameType? = null
) {
    val currentRound: Int get() = gameSession.round
    val currentGameType: GameType get() = gameSession.gameType
    val hasPlayers: Boolean get() = players.isNotEmpty()
    val hasScores: Boolean get() = players.any { it.score > 0 }
} 