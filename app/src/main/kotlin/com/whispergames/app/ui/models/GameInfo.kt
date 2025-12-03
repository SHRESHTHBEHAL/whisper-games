package com.whispergames.app.ui.models

/**
 * Data class representing a game in the carousel
 */
data class GameInfo(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val difficulty: String,
    val backgroundColor: String,
    var highScore: String = "0"
)
