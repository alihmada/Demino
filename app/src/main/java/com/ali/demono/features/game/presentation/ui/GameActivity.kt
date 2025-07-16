package com.ali.demono.features.game.presentation.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ali.demono.R
import com.ali.demono.core.extensions.enableEdgeToEdgeDisplay
import com.ali.demono.databinding.ActivityGameBinding
import com.ali.demono.features.game.domain.model.GameType
import com.ali.demono.features.game.presentation.state.GameUiEvent
import com.ali.demono.features.game.presentation.state.GameUiState
import com.ali.demono.features.game.presentation.ui.adapter.PlayerAdapter
import com.ali.demono.features.game.presentation.ui.manager.GameUiManager
import com.ali.demono.features.game.presentation.viewmodel.GameViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.util.Log

@AndroidEntryPoint
class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding
    private val viewModel: GameViewModel by viewModels()
    private lateinit var playerAdapter: PlayerAdapter
    private lateinit var uiManager: GameUiManager
    private var isUpdatingGameTypeProgrammatically = false
    private var lastShownGameTypeDialog: GameType? = null
    private var lastShownNextRoundDialog = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBinding()
        enableEdgeToEdgeDisplay(binding.root)
        setupUiManager()
        setupRecyclerView()
        setupGameTypeSelection()
        setupObservers()
        setupClickListeners()
        //showSwipeInstructions()
    }

    private fun setupBinding() {
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
    }

    private fun setupUiManager() {
        uiManager = GameUiManager(this)
    }

    private fun setupRecyclerView() {
        playerAdapter = PlayerAdapter(
            onScoreUpdate = { playerId, scoreChange ->
                viewModel.handleEvent(GameUiEvent.UpdatePlayerScore(playerId, scoreChange))
            },
            onAddScore = { playerId ->
                uiManager.showAddScoreDialog(playerId) { id, score ->
                    viewModel.handleEvent(GameUiEvent.UpdatePlayerScore(id, score))
                }
            },
            onEditPlayer = { playerId, currentName, currentScore ->
                uiManager.showEditPlayerDialog(
                    playerId = playerId,
                    currentName = currentName,
                    currentScore = currentScore
                ) { id, newName, newScore ->
                    viewModel.handleEvent(GameUiEvent.EditPlayer(id, newName, newScore))
                }
            },
            onDeletePlayer = { playerId ->
                uiManager.showDeletePlayerDialog(playerId) { id ->
                    viewModel.handleEvent(GameUiEvent.DeletePlayer(id))
                    playerAdapter.showUndoSnackbarForDeletedPlayer()
                }
            },
            onUndoDelete = { player ->
                viewModel.restorePlayer(player)
            }
        )

        // Anchor Snackbars to the FAB
        playerAdapter.setAnchorFab(binding.addPlayerEfab)

        binding.playersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@GameActivity)
            adapter = playerAdapter
        }

        playerAdapter.setupSwipeGestures(binding.playersRecyclerView)
    }

    private fun setupGameTypeSelection() {
        binding.gameTypeChipGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked && !isUpdatingGameTypeProgrammatically) {
                val gameType = when (checkedId) {
                    R.id.chipMasri -> GameType.MASRI
                    R.id.chipAmerican -> GameType.AMERICAN
                    R.id.chipTeams -> GameType.TEAMS
                    else -> GameType.MASRI
                }
                // Do not revert selection here!
                viewModel.handleEvent(GameUiEvent.SetGameType(gameType))
            }
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                Log.d("RX", "uiState collected: $state")
                updateUi(state)
            }
        }
    }

    private fun updateUi(state: GameUiState) {
        updatePlayerList(state)
        updateGameInfo(state)
        updateGameTypeSelection(state)
        handleUiEvents(state)
    }

    private fun updatePlayerList(state: GameUiState) {
        playerAdapter.updatePlayers(state.players)
        playerAdapter.updateGameType(state.currentGameType)
    }

    private fun updateGameInfo(state: GameUiState) {
        binding.roundInfoText.text = getString(
            R.string.round_format,
            state.currentRound
        )
    }

    private fun updateGameTypeSelection(state: GameUiState) {
        val buttonId = when (state.currentGameType) {
            GameType.MASRI -> R.id.chipMasri
            GameType.AMERICAN -> R.id.chipAmerican
            GameType.TEAMS -> R.id.chipTeams
        }
        isUpdatingGameTypeProgrammatically = true
        try {
            binding.gameTypeChipGroup.check(buttonId)
        } finally {
            isUpdatingGameTypeProgrammatically = false
        }
    }

    private fun handleUiEvents(state: GameUiState) {
        state.error?.let { error ->
            showError(error)
        }

        state.showPlayerLimitMessage?.let { gameType ->
            val message = gameType.getPlayerLimitMessage(this)
            uiManager.showPlayerLimitMessage(binding.root, message)
            viewModel.handleEvent(GameUiEvent.ClearPlayerLimitMessage)
        }

        state.showNextRoundConfirmation.let { show ->
            if (show && !lastShownNextRoundDialog) {
                lastShownNextRoundDialog = true
                Log.d("RX", "showNextRoundDialog shown")
                uiManager.showNextRoundDialog(
                    onNextRoundConfirmed = {
                        Log.d("RX", "NextRoundDialog confirmed")
                        viewModel.handleEvent(GameUiEvent.ConfirmNextRound)
                    },
                    onNextRoundCancelled = {
                        Log.d("RX", "NextRoundDialog cancelled")
                        viewModel.handleEvent(GameUiEvent.CancelNextRound)
                    }
                )
            } else if (!show) {
                lastShownNextRoundDialog = false
            }
        }

        state.showGameTypeChangeDialog?.let { gameType ->
            if (gameType != lastShownGameTypeDialog) {
                lastShownGameTypeDialog = gameType
                Log.d("RX", "showGameTypeChangeDialog set: $gameType")
                uiManager.showGameTypeChangeDialog(gameType,
                    onGameTypeChanged = { newGameType ->
                        Log.d("RX", "GameTypeChangeDialog confirmed: $newGameType")
                        viewModel.handleEvent(GameUiEvent.ConfirmGameTypeChange(newGameType))
                    },
                    onCancelled = {
                        Log.d("RX", "GameTypeChangeDialog cancelled by user")
                        viewModel.handleEvent(GameUiEvent.CancelGameTypeChange)
                    }
                )
            }
        } ?: run {
            lastShownGameTypeDialog = null
            Log.d("RX", "GameTypeChangeDialog cancelled")
        }
    }

    private fun setupClickListeners() {
        binding.addPlayerEfab.setOnClickListener {
            Log.d("RX", "Add Player FAB clicked")
            uiManager.showAddPlayerDialog { playerName ->
                Log.d("RX", "Add Player dialog callback called with: $playerName")
                if (playerName.isNotBlank()) {
                    Log.d("RX", "Calling handleEvent for AddPlayer: $playerName")
                    viewModel.handleEvent(GameUiEvent.AddPlayer(playerName))
                }
            }
        }
        binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_reset_game -> {
                    uiManager.showResetGameDialog {
                        viewModel.handleEvent(GameUiEvent.ResetGame)
                    }
                    true
                }
                R.id.action_next_round -> {
                    uiManager.showNextRoundDialog(
                        onNextRoundConfirmed = {
                            viewModel.handleEvent(GameUiEvent.ConfirmNextRound)
                        },
                        onNextRoundCancelled = {
                            viewModel.handleEvent(GameUiEvent.CancelNextRound)
                        }
                    )
                    true
                }
                else -> false
            }
        }
    }

    private fun showSwipeInstructions() {
        uiManager.showSwipeInstructions(binding.root)
    }

    private fun showError(error: String) {
        // Handle error display
    }
}