package com.ali.demono.features.game.domain.usecase

import com.ali.demono.features.game.domain.repository.GameRepository

class UpdateScoreUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(playerId: String, score: Int) {
        repository.updatePlayerScore(playerId, score)
    }
} 