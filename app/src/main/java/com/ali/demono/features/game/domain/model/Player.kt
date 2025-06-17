package com.ali.demono.features.game.domain.model

import java.util.UUID

data class Player(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val score: Int = 0
) 