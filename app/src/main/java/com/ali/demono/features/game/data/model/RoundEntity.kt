package com.ali.demono.features.game.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rounds")
data class RoundEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val roundNumber: Int,
    val gameType: String,
    val timestamp: Long = System.currentTimeMillis()
) 