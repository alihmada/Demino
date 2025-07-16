package com.ali.demono.di

import android.content.Context
import com.ali.demono.features.game.data.database.GameDatabase
import com.ali.demono.features.game.data.repository.RoomGameRepository
import com.ali.demono.features.game.domain.repository.GameRepository
import com.ali.demono.features.game.domain.usecase.AddPlayerUseCase
import com.ali.demono.features.game.domain.usecase.NextRoundUseCase
import com.ali.demono.features.game.domain.usecase.ResetGameUseCase
import com.ali.demono.features.game.domain.usecase.SetGameTypeUseCase
import com.ali.demono.features.game.domain.usecase.UpdateScoreUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGameDatabase(@ApplicationContext context: Context): GameDatabase {
        return GameDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideGameRepository(database: GameDatabase): GameRepository {
        return RoomGameRepository(database)
    }

    @Provides
    @Singleton
    fun provideAddPlayerUseCase(repository: GameRepository): AddPlayerUseCase {
        return AddPlayerUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateScoreUseCase(repository: GameRepository): UpdateScoreUseCase {
        return UpdateScoreUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideResetGameUseCase(repository: GameRepository): ResetGameUseCase {
        return ResetGameUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSetGameTypeUseCase(repository: GameRepository): SetGameTypeUseCase {
        return SetGameTypeUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideNextRoundUseCase(repository: GameRepository): NextRoundUseCase {
        return NextRoundUseCase(repository)
    }
} 