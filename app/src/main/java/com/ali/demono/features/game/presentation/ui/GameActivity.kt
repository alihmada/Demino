package com.ali.demono.features.game.presentation.ui

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ali.demono.R
import com.ali.demono.features.game.domain.model.GameType
import com.ali.demono.features.game.presentation.viewmodel.GameViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GameActivity : AppCompatActivity() {
    
    private val viewModel: GameViewModel by viewModels()
    private lateinit var playerAdapter: PlayerAdapter
    
    // UI Components
    private lateinit var gameTypeChipGroup: ChipGroup
    private lateinit var playerNameInput: EditText
    private lateinit var addPlayerButton: MaterialButton
    private lateinit var playersRecyclerView: RecyclerView
    private lateinit var nextRoundButton: MaterialButton
    private lateinit var resetGameButton: MaterialButton
    private lateinit var roundInfoText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game)

        setupEdgeToEdge()
        initViews()
        setupRecyclerView()
        setupClickListeners()
        setupObservers()
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        gameTypeChipGroup = findViewById(R.id.gameTypeChipGroup)
        playerNameInput = findViewById(R.id.playerNameInput)
        addPlayerButton = findViewById(R.id.addPlayerButton)
        playersRecyclerView = findViewById(R.id.playersRecyclerView)
        nextRoundButton = findViewById(R.id.nextRoundButton)
        resetGameButton = findViewById(R.id.resetGameButton)
        roundInfoText = findViewById(R.id.roundInfoText)
    }
    
    private fun setupRecyclerView() {
        playerAdapter = PlayerAdapter(
            onScoreUpdate = { playerId, newScore ->
                viewModel.updateScore(playerId, newScore)
            },
            onAddScore = { playerId ->
                showAddScoreDialog(playerId)
            }
        )
        
        playersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@GameActivity)
            adapter = playerAdapter
        }
    }
    
    private fun setupClickListeners() {
        addPlayerButton.setOnClickListener {
            val playerName = playerNameInput.text.toString().trim()
            if (playerName.isNotEmpty()) {
                viewModel.addPlayer(playerName)
                playerNameInput.text.clear()
            }
        }
        
        gameTypeChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val gameType = when (checkedIds.firstOrNull()) {
                R.id.chipMasri -> GameType.MASRI
                R.id.chipAmerican -> GameType.AMERICAN
                R.id.chipTeams -> GameType.TEAMS
                else -> GameType.MASRI
            }
            viewModel.setGameType(gameType)
        }
        
        nextRoundButton.setOnClickListener {
            viewModel.nextRound()
        }
        
        resetGameButton.setOnClickListener {
            showResetConfirmationDialog()
        }
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.gameSession.collect { gameSession ->
                updateGameTypeUI(gameSession.gameType)
                updateRoundInfo(gameSession.round)
            }
        }
        
        lifecycleScope.launch {
            viewModel.players.collect { players ->
                updatePlayersList(players)
            }
        }
        
        lifecycleScope.launch {
            viewModel.showPlayerLimitMessage.collect { message ->
                message?.let {
                    showPlayerLimitSnackBar(it)
                    viewModel.clearPlayerLimitMessage()
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.showGameTypeChangeDialog.collect { newGameType ->
                newGameType?.let {
                    showGameTypeChangeConfirmationDialog(it)
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.showNextRoundConfirmation.collect { showDialog ->
                if (showDialog) {
                    showNextRoundConfirmationDialog()
                }
            }
        }
    }
    
    private fun updateGameTypeUI(gameType: GameType) {
        val chipId = when (gameType) {
            GameType.MASRI -> R.id.chipMasri
            GameType.AMERICAN -> R.id.chipAmerican
            GameType.TEAMS -> R.id.chipTeams
        }
        gameTypeChipGroup.check(chipId)
        playerAdapter.updateGameType(gameType)
    }
    
    private fun updatePlayersList(players: List<com.ali.demono.features.game.domain.model.Player>) {
        playerAdapter.updatePlayers(players)
    }
    
    private fun updateRoundInfo(round: Int) {
        roundInfoText.text = getString(R.string.round_format, round)
    }
    
    private fun showPlayerLimitSnackBar(message: String) {
        Snackbar.make(
            findViewById(R.id.main),
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }
    
    private fun showGameTypeChangeConfirmationDialog(newGameType: GameType) {
        val gameTypeName = when (newGameType) {
            GameType.MASRI -> getString(R.string.game_type_masri)
            GameType.AMERICAN -> getString(R.string.game_type_american)
            GameType.TEAMS -> getString(R.string.game_type_teams)
        }
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.change_game_type_title))
            .setMessage(getString(R.string.change_game_type_message, gameTypeName))
            .setPositiveButton(getString(R.string.yes_reset)) { _, _ ->
                viewModel.confirmGameTypeChange()
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                viewModel.cancelGameTypeChange()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showAddScoreDialog(playerId: String) {
        val scoreInput = EditText(this).apply {
            hint = getString(R.string.enter_score)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.add_score))
            .setView(scoreInput)
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                val scoreText = scoreInput.text.toString()
                if (scoreText.isNotEmpty()) {
                    val score = scoreText.toIntOrNull() ?: 0
                    viewModel.updateScore(playerId, score)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun showResetConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.reset_confirmation_title))
            .setMessage(getString(R.string.reset_confirmation_message))
            .setPositiveButton(getString(R.string.reset)) { _, _ ->
                viewModel.resetGame()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun showNextRoundConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.next_round_confirmation_title))
            .setMessage(getString(R.string.next_round_confirmation_message))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.confirmNextRound()
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                viewModel.cancelNextRound()
            }
            .setCancelable(false)
            .show()
    }
} 