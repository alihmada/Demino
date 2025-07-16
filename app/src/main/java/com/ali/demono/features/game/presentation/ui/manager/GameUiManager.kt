package com.ali.demono.features.game.presentation.ui.manager

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.ali.demono.R
import com.ali.demono.features.game.domain.model.GameType
import com.ali.demono.features.game.presentation.ui.helper.MaterialDialogHelper
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.util.Log

/**
 * Manages UI interactions and dialogs for the game screen
 * Follows clean architecture principles with proper separation of concerns
 */
class GameUiManager(private val context: Context) {

    fun showSwipeInstructions(rootView: View) {
        Snackbar.make(
            rootView,
            context.getString(R.string.swipe_instructions),
            Snackbar.LENGTH_LONG
        ).show()
    }

    fun showPlayerLimitMessage(rootView: View, message: String) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show()
    }

    fun showAddPlayerDialog(
        onPlayerAdded: (String) -> Unit
    ) {
        Log.d("RX", "showAddPlayerDialog called")
        val inputView = createNameInputLayout() // create ONCE
        MaterialDialogHelper.showInputDialog(
            context = context,
            title = context.getString(R.string.add_player),
            customView = inputView,
            positiveButtonText = context.getString(R.string.add),
            negativeButtonText = context.getString(R.string.cancel),
            onPositiveClick = {
                val playerName = getInputText(inputView)?.trim() // use the same view
                Log.d("RX", "Add Player dialog positive click, playerName: $playerName")
                if (!playerName.isNullOrEmpty()) {
                    Log.d("RX", "onPlayerAdded callback called with playerName: $playerName")
                    onPlayerAdded(playerName)
                }
            }
        )
    }

    fun showAddScoreDialog(
        playerId: String,
        onScoreAdded: (String, Int) -> Unit
    ) {
        val inputLayout = createScoreInputLayout()

        MaterialDialogHelper.showInputDialog(
            context = context,
            title = context.getString(R.string.add_score),
            customView = inputLayout,
            positiveButtonText = context.getString(R.string.add),
            negativeButtonText = context.getString(R.string.cancel),
            onPositiveClick = {
                val scoreText = getInputText(inputLayout)
                if (!scoreText.isNullOrEmpty()) {
                    val score = scoreText.toIntOrNull() ?: 0
                    onScoreAdded(playerId, score)
                }
            }
        )
    }

    fun showEditPlayerDialog(
        playerId: String,
        currentName: String,
        currentScore: Int,
        onPlayerEdited: (String, String, Int) -> Unit
    ) {
        val layout = createEditPlayerLayout(currentName, currentScore)

        MaterialDialogHelper.showInputDialog(
            context = context,
            title = context.getString(R.string.edit_player),
            customView = layout,
            positiveButtonText = context.getString(R.string.save),
            negativeButtonText = context.getString(R.string.cancel),
            onPositiveClick = {
                val newName = getEditPlayerName(layout)?.trim()
                val newScoreText = getEditPlayerScore(layout)

                if (!newName.isNullOrEmpty() && !newScoreText.isNullOrEmpty()) {
                    val newScore = newScoreText.toIntOrNull() ?: currentScore
                    onPlayerEdited(playerId, newName, newScore)
                }
            }
        )
    }

    fun showDeletePlayerDialog(
        playerId: String,
        onPlayerDeleted: (String) -> Unit
    ) {
        MaterialDialogHelper.showConfirmationDialog(
            context = context,
            title = context.getString(R.string.delete_confirmation_title),
            message = context.getString(R.string.delete_confirmation_message),
            positiveButtonText = context.getString(R.string.delete),
            negativeButtonText = context.getString(R.string.cancel),
            onPositiveClick = {
                onPlayerDeleted(playerId)
            }
        )
    }

    fun showResetGameDialog(
        onGameReset: () -> Unit
    ) {
        MaterialDialogHelper.showConfirmationDialog(
            context = context,
            title = context.getString(R.string.reset_confirmation_title),
            message = context.getString(R.string.reset_confirmation_message),
            positiveButtonText = context.getString(R.string.reset),
            negativeButtonText = context.getString(R.string.cancel),
            onPositiveClick = {
                onGameReset()
            }
        )
    }

    fun showNextRoundDialog(
        onNextRoundConfirmed: () -> Unit,
        onNextRoundCancelled: () -> Unit
    ) {
        MaterialDialogHelper.showConfirmationDialog(
            context = context,
            title = context.getString(R.string.next_round_confirmation_title),
            message = context.getString(R.string.next_round_confirmation_message),
            positiveButtonText = context.getString(R.string.yes),
            negativeButtonText = context.getString(R.string.cancel),
            onPositiveClick = {
                onNextRoundConfirmed()
            },
            onNegativeClick = {
                onNextRoundCancelled()
            },
            cancelable = false
        )
    }

    fun showGameTypeChangeDialog(
        newGameType: GameType,
        onGameTypeChanged: (GameType) -> Unit,
        onCancelled: () -> Unit
    ) {
        Log.d("RX", "showGameTypeChangeDialog called with newGameType: $newGameType")
        val gameTypeName = getGameTypeName(newGameType)
        MaterialDialogHelper.showConfirmationDialog(
            context = context,
            title = context.getString(R.string.change_game_type_title),
            message = context.getString(R.string.change_game_type_message, gameTypeName),
            positiveButtonText = context.getString(R.string.yes_reset),
            negativeButtonText = context.getString(R.string.cancel),
            onPositiveClick = {
                Log.d("RX", "GameTypeChangeDialog confirmed for $newGameType")
                onGameTypeChanged(newGameType)
            },
            onNegativeClick = {
                Log.d("RX", "GameTypeChangeDialog cancelled for $newGameType")
                onCancelled()
            }
        )
    }

    private fun createNameInputLayout(): TextInputLayout {
        return TextInputLayout(context).apply {
            hint = context.getString(R.string.player_name_hint)
            addView(TextInputEditText(context))
            setPadding(32, 24, 32, 8)
        }
    }

    private fun createScoreInputLayout(): TextInputLayout {
        return TextInputLayout(context).apply {
            hint = context.getString(R.string.enter_score)
            addView(TextInputEditText(context).apply {
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
            })
            setPadding(32, 24, 32, 8)
        }
    }

    private fun createEditPlayerLayout(currentName: String, currentScore: Int): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 8)

            // Name input
            val nameInputLayout = TextInputLayout(context).apply {
                hint = context.getString(R.string.player_name_hint)
                setPadding(0, 0, 0, 24) // Increased bottom margin
            }
            val nameInput = TextInputEditText(nameInputLayout.context).apply {
                setText(currentName)
                setSelection(text?.length ?: 0) // Place cursor at end
            }
            nameInputLayout.addView(nameInput)
            addView(nameInputLayout)

            // Score input
            val scoreInputLayout = TextInputLayout(context).apply {
                hint = context.getString(R.string.enter_score)
                setPadding(0, 0, 0, 8) // Add bottom margin
            }
            val scoreInput = TextInputEditText(scoreInputLayout.context).apply {
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                setText(currentScore.toString())
                setSelection(text?.length ?: 0) // Place cursor at end
            }
            scoreInputLayout.addView(scoreInput)
            addView(scoreInputLayout)
        }
    }

    private fun getInputText(inputLayout: TextInputLayout): String? {
        // Find the TextInputEditText within the TextInputLayout recursively
        return findTextInputEditText(inputLayout)
    }

    private fun findTextInputEditText(view: View): String? {

        if (view is TextInputEditText) {
            val text = view.text?.toString()
            return text
        }

        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                val result = findTextInputEditText(child)
                if (result != null) {
                    return result
                }
            }
        }

        return null
    }

    private fun getEditPlayerName(layout: LinearLayout): String? {
        val nameInputLayout = layout.getChildAt(0) as? TextInputLayout
        return nameInputLayout?.let { getInputText(it) }
    }

    private fun getEditPlayerScore(layout: LinearLayout): String? {
        val scoreInputLayout = layout.getChildAt(1) as? TextInputLayout
        return scoreInputLayout?.let { getInputText(it) }
    }

    private fun getGameTypeName(gameType: GameType): String {
        return when (gameType) {
            GameType.MASRI -> context.getString(R.string.game_type_masri)
            GameType.AMERICAN -> context.getString(R.string.game_type_american)
            GameType.TEAMS -> context.getString(R.string.game_type_teams)
        }
    }
} 