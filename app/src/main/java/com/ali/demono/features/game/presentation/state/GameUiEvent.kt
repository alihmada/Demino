package com.ali.demono.features.game.presentation.state

import com.ali.demono.features.game.domain.model.GameType

/**
 * Represents user events/actions in the game UI
 */
sealed class GameUiEvent {
    data class AddPlayer(val name: String) : GameUiEvent()
    data class UpdatePlayerScore(val playerId: String, val score: Int) : GameUiEvent()
    data class EditPlayer(val playerId: String, val newName: String, val newScore: Int) :
        GameUiEvent()

    data class DeletePlayer(val playerId: String) : GameUiEvent()
    data class SetGameType(val gameType: GameType) : GameUiEvent()
    data class ConfirmGameTypeChange(val gameType: GameType) : GameUiEvent()
    object ResetGame : GameUiEvent()
    object NextRound : GameUiEvent()
    object ConfirmNextRound : GameUiEvent()
    object CancelNextRound : GameUiEvent()
    object ClearPlayerLimitMessage : GameUiEvent()
    object CancelGameTypeChange : GameUiEvent()
} 