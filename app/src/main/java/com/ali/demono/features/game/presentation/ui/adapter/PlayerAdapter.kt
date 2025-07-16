package com.ali.demono.features.game.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ali.demono.R
import com.ali.demono.features.game.domain.model.GameType
import com.ali.demono.features.game.domain.model.Player
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar

/**
 * Adapter for displaying players in a RecyclerView
 * Follows clean architecture principles with proper separation of concerns
 */
class PlayerAdapter(
    private var players: List<Player> = emptyList(),
    private var gameType: GameType = GameType.MASRI,
    private val onScoreUpdate: (String, Int) -> Unit = { _, _ -> },
    private val onAddScore: (String) -> Unit = { },
    private val onEditPlayer: (String, String, Int) -> Unit = { _, _, _ -> },
    private val onDeletePlayer: (String) -> Unit = { },
    private val onUndoDelete: (Player) -> Unit = { }
) : RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {

    private var deletedPlayer: Player? = null
    private var deletedPosition: Int = -1
    private var itemTouchHelper: ItemTouchHelper? = null
    private var attachedRecyclerView: RecyclerView? = null
    // Add a property to hold the FAB reference
    private var anchorFab: View? = null

    fun setAnchorFab(fab: View) {
        anchorFab = fab
    }

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerNameText: TextView = itemView.findViewById(R.id.playerNameText)
        val playerScoreText: TextView = itemView.findViewById(R.id.playerScoreText)
        val minusButton: MaterialButton = itemView.findViewById(R.id.minusButton)
        val plusButton: MaterialButton = itemView.findViewById(R.id.plusButton)
        val addScoreButton: MaterialButton = itemView.findViewById(R.id.addScoreButton)
        val scoreControlsLayoutA: View = itemView.findViewById(R.id.scoreControlsLayoutA)
        val scoreControlsLayoutMT: View = itemView.findViewById(R.id.scoreControlsLayoutMT)
        val scoreControlsText: TextView = itemView.findViewById(R.id.scoreControlsText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = players[position]
        bindPlayerData(holder, player)
        setupGameTypeSpecificControls(holder, player)
    }

    private fun bindPlayerData(holder: PlayerViewHolder, player: Player) {
        holder.playerNameText.text = player.name
        holder.playerScoreText.text = player.score.toString()
    }

    private fun setupGameTypeSpecificControls(holder: PlayerViewHolder, player: Player) {
        when (gameType) {
            GameType.MASRI -> setupMasriControls(holder, player)
            GameType.AMERICAN -> setupAmericanControls(holder, player)
            GameType.TEAMS -> setupTeamsControls(holder, player)
        }
    }

    private fun setupMasriControls(holder: PlayerViewHolder, player: Player) {
        holder.scoreControlsLayoutA.visibility = View.GONE
        holder.scoreControlsLayoutMT.visibility = View.VISIBLE
        holder.addScoreButton.setOnClickListener {
            onAddScore(player.id)
        }
    }

    private fun setupAmericanControls(holder: PlayerViewHolder, player: Player) {
        holder.scoreControlsLayoutA.visibility = View.VISIBLE
        holder.scoreControlsLayoutMT.visibility = View.GONE
        holder.scoreControlsText.text = player.score.toString()

        holder.minusButton.setOnClickListener {
            onScoreUpdate(player.id, -1)
        }

        holder.plusButton.setOnClickListener {
            onScoreUpdate(player.id, 1)
        }
    }

    private fun setupTeamsControls(holder: PlayerViewHolder, player: Player) {
        holder.scoreControlsLayoutA.visibility = View.GONE
        holder.scoreControlsLayoutMT.visibility = View.VISIBLE
        holder.addScoreButton.setOnClickListener {
            onAddScore(player.id)
        }
    }

    override fun getItemCount(): Int = players.size

    fun updatePlayers(newPlayers: List<Player>) {
        players = newPlayers
        notifyDataSetChanged()
    }

    fun updateGameType(newGameType: GameType) {
        gameType = newGameType
        notifyDataSetChanged()
    }

    fun showUndoSnackbarForDeletedPlayer() {
        deletedPlayer?.let { player ->
            showUndoSnackBar(player)
        }
    }

    fun setupSwipeGestures(recyclerView: RecyclerView) {
        attachedRecyclerView = recyclerView
        val swipeHandler = createSwipeHandler()
        itemTouchHelper = ItemTouchHelper(swipeHandler).apply {
            attachToRecyclerView(recyclerView)
        }
    }

    private fun createSwipeHandler(): ItemTouchHelper.SimpleCallback {
        return object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                attachedRecyclerView?.let { recyclerView ->
                    handleSwipeAction(viewHolder, direction)
                }
            }

            override fun onChildDraw(
                c: android.graphics.Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                drawSwipeBackground(c, recyclerView, viewHolder, dX)
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
    }

    private fun handleSwipeAction(
        viewHolder: RecyclerView.ViewHolder,
        direction: Int
    ) {
        val position = viewHolder.adapterPosition
        if (position >= 0 && position < players.size) {
            val player = players[position]

            when (direction) {
                ItemTouchHelper.LEFT -> {
                    handleDeleteSwipe(player)
                    // If delete dialog is cancelled, reset the item
                    notifyItemChanged(position)
                }
                ItemTouchHelper.RIGHT -> {
                    handleEditSwipe(player)
                    // If edit dialog is cancelled, reset the item
                    notifyItemChanged(position)
                }
            }
        }
    }

    private fun handleDeleteSwipe(player: Player) {
        deletedPlayer = player
        deletedPosition = players.indexOf(player)
        onDeletePlayer(player.id)
    }

    private fun handleEditSwipe(player: Player) {
        onEditPlayer(player.id, player.name, player.score)
    }

    private fun showUndoSnackBar(player: Player) {
        attachedRecyclerView?.let { recyclerView ->
            val context = recyclerView.context
            val rootView = recyclerView.rootView
            val snackbar = Snackbar.make(
                rootView,
                context.getString(R.string.player_deleted_message, player.name),
                Snackbar.LENGTH_LONG
            )
            snackbar.setAction(context.getString(R.string.undo)) {
                deletedPlayer?.let { player ->
                    onUndoDelete(player)
                }
            }
            anchorFab?.let { snackbar.setAnchorView(it) }
            snackbar.show()
        }
    }

    private fun drawSwipeBackground(
        c: android.graphics.Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float
    ) {
        val itemView = viewHolder.itemView
        val context = recyclerView.context

        when {
            dX > 0 -> drawEditSwipeBackground(c, itemView, context, dX)
            dX < 0 -> drawDeleteSwipeBackground(c, itemView, context, dX)
        }
    }

    private fun drawEditSwipeBackground(
        c: android.graphics.Canvas,
        itemView: View,
        context: android.content.Context,
        dX: Float
    ) {
        val background = context.getColor(R.color.md_theme_tertiary).toDrawable()
        background.setBounds(
            itemView.left,
            itemView.top,
            itemView.left + dX.toInt(),
            itemView.bottom
        )
        background.draw(c)

        drawEditIcon(c, itemView, context)
        itemView.translationX = 0f
    }

    private fun drawDeleteSwipeBackground(
        c: android.graphics.Canvas,
        itemView: View,
        context: android.content.Context,
        dX: Float
    ) {
        val background = context.getColor(R.color.md_theme_error).toDrawable()
        background.setBounds(
            itemView.right + dX.toInt(),
            itemView.top,
            itemView.right,
            itemView.bottom
        )
        background.draw(c)

        drawDeleteIcon(c, itemView, context)
    }

    private fun drawEditIcon(c: android.graphics.Canvas, itemView: View, context: android.content.Context) {
        val editIcon = AppCompatResources.getDrawable(context, R.drawable.edit)
        editIcon?.let {
            val iconMargin = (itemView.height - it.intrinsicHeight) / 2
            val iconTop = itemView.top + iconMargin
            val iconBottom = iconTop + it.intrinsicHeight
            val iconLeft = itemView.left + 32
            val iconRight = iconLeft + it.intrinsicWidth
            it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            it.draw(c)
        }
    }

    private fun drawDeleteIcon(c: android.graphics.Canvas, itemView: View, context: android.content.Context) {
        val deleteIcon = AppCompatResources.getDrawable(context, R.drawable.delete)
        deleteIcon?.let {
            val iconMargin = (itemView.height - it.intrinsicHeight) / 2
            val iconTop = itemView.top + iconMargin
            val iconBottom = iconTop + it.intrinsicHeight
            val iconRight = itemView.right - 32
            val iconLeft = iconRight - it.intrinsicWidth
            it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            it.draw(c)
        }
    }
}