package com.ali.demono.features.game.domain.usecase

import com.ali.demono.features.game.domain.model.Player
import com.ali.demono.features.game.domain.repository.GameRepository

class AddPlayerUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(player: Player) {
        repository.addPlayer(player)
    }
} 