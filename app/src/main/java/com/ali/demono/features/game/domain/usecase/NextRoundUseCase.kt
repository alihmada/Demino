package com.ali.demono.features.game.domain.usecase

import com.ali.demono.features.game.domain.repository.GameRepository

class NextRoundUseCase(private val repository: GameRepository) {
    suspend operator fun invoke() {
        repository.nextRound()
    }
} 