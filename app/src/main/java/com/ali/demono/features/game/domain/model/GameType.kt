package com.ali.demono.features.game.domain.model

enum class GameType(val maxPlayers: Int, val isTeamGame: Boolean = false) {
    MASRI(4, false),
    AMERICAN(4, false),
    TEAMS(4, true); // 2 teams of 2 players each
    
    fun canAddPlayer(currentPlayerCount: Int): Boolean {
        return if (isTeamGame) {
            // For team games, we need even number of players and max 4 total
            currentPlayerCount < maxPlayers && currentPlayerCount <= 1
        } else {
            currentPlayerCount < maxPlayers
        }
    }
    
    fun getMaxTeams(): Int {
        return if (isTeamGame) 2 else 0
    }
    
    fun getPlayerLimitMessage(): String {
        return if (isTeamGame) {
            "Maximum 2 teams (4 players total)"
        } else {
            "Maximum $maxPlayers players"
        }
    }
} 