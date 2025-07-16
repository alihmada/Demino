package com.ali.demono.features.game.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ali.demono.features.game.data.dao.GameSessionDao
import com.ali.demono.features.game.data.dao.PlayerDao
import com.ali.demono.features.game.data.dao.PlayerScoreDao
import com.ali.demono.features.game.data.model.GameSessionEntity
import com.ali.demono.features.game.data.model.PlayerEntity
import com.ali.demono.features.game.data.model.PlayerScoreEntity

@Database(
    entities = [
        PlayerEntity::class,
        GameSessionEntity::class,
        PlayerScoreEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun gameSessionDao(): GameSessionDao
    abstract fun playerScoreDao(): PlayerScoreDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getDatabase(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "game_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 