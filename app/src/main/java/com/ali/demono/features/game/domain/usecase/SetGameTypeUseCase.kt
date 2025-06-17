package com.ali.demono.features.game.domain.usecase

import com.ali.demono.features.game.domain.model.GameType
import com.ali.demono.features.game.domain.repository.GameRepository

class SetGameTypeUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(type: GameType) {
        repository.setGameType(type)
    }
} 