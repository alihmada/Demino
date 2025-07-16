package com.ali.demono.features.game.domain.model

import android.content.Context

enum class GameType(val maxPlayers: Int, val isTeamGame: Boolean = false) {
    MASRI(4, false),
    AMERICAN(4, false),
    TEAMS(4, true); // 2 teams of 2 players each

    fun canAddPlayer(currentPlayerCount: Int): Boolean {
        return if (isTeamGame) {
            // For team games, we need even number of players and max 4 total
            currentPlayerCount < getMaxTeams()
        } else {
            currentPlayerCount < maxPlayers
        }
    }

    fun getMaxTeams(): Int {
        return if (isTeamGame) 2 else 0
    }

    fun getPlayerLimitMessage(context: Context): String {
        return if (isTeamGame) {
            context.getString(com.ali.demono.R.string.player_limit_teams)
        } else {
            context.getString(com.ali.demono.R.string.player_limit_generic, maxPlayers)
        }
    }
} 