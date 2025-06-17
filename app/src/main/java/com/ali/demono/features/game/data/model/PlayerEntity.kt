package com.ali.demono.features.game.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val currentScore: Int = 0,
    val roundNumber: Int = 1
) 