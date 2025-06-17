package com.ali.demono.features.game.domain.usecase

import com.ali.demono.features.game.domain.repository.GameRepository

class ResetGameUseCase(private val repository: GameRepository) {
    suspend operator fun invoke() {
        repository.resetGame()
    }
} 