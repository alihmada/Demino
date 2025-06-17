package com.ali.demono.features.game.presentation.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ali.demono.R
import com.ali.demono.features.game.domain.model.GameType
import com.ali.demono.features.game.domain.model.Player
import com.google.android.material.button.MaterialButton

class PlayerAdapter(
    private var players: List<Player> = emptyList(),
    private var gameType: GameType = GameType.MASRI,
    private val onScoreUpdate: (String, Int) -> Unit = { _, _ -> },
    private val onAddScore: (String) -> Unit = { }
) : RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerNameText: TextView = itemView.findViewById(R.id.playerNameText)
        val playerScoreText: TextView = itemView.findViewById(R.id.playerScoreText)
        val minusButton: MaterialButton = itemView.findViewById(R.id.minusButton)
        val plusButton: MaterialButton = itemView.findViewById(R.id.plusButton)
        val addScoreButton: MaterialButton = itemView.findViewById(R.id.addScoreButton)
        val scoreControlsLayout: View = itemView.findViewById(R.id.scoreControlsLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = players[position]
        
        holder.playerNameText.text = player.name
        holder.playerScoreText.text = player.score.toString()
        
        when (gameType) {
            GameType.MASRI -> {
                holder.scoreControlsLayout.visibility = View.GONE
                holder.addScoreButton.visibility = View.VISIBLE
                holder.addScoreButton.setOnClickListener {
                    onAddScore(player.id)
                }
            }
            GameType.AMERICAN -> {
                holder.scoreControlsLayout.visibility = View.VISIBLE
                holder.addScoreButton.visibility = View.GONE
                
                holder.minusButton.setOnClickListener {
                    onScoreUpdate(player.id, -1)
                }
                
                holder.plusButton.setOnClickListener {
                    onScoreUpdate(player.id, 1)
                }
            }
            GameType.TEAMS -> {
                holder.scoreControlsLayout.visibility = View.GONE
                holder.addScoreButton.visibility = View.VISIBLE
                holder.addScoreButton.setOnClickListener {
                    onAddScore(player.id)
                }
            }
        }
    }

    override fun getItemCount(): Int = players.size

    fun updatePlayers(newPlayers: List<Player>) {
        println("DEBUG: PlayerAdapter updatePlayers - new players: ${newPlayers.map { "${it.name}: ${it.score}" }}")
        players = newPlayers
        notifyDataSetChanged()
    }

    fun updateGameType(newGameType: GameType) {
        gameType = newGameType
        notifyDataSetChanged()
    }
}