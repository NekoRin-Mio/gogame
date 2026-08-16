package com.gogame.data

/**
 * 游戏模式
 */
enum class GameMode {
    PVP,    // 人对人
    PVE     // 人机
}

/**
 * 棋盘大小选项
 */
enum class BoardSize(val size: Int, val displayName: String) {
    SMALL(9, "9 × 9"),
    MEDIUM(13, "13 × 13"),
    LARGE(19, "19 × 19")
}

/**
 * 难度
 */
enum class AIDifficulty(val displayName: String, val searchDepth: Int) {
    EASY("简单", 1),
    NORMAL("普通", 2),
    HARD("困难", 3)
}

/**
 * 游戏设置
 */
data class GameSettings(
    val boardSize: BoardSize = BoardSize.LARGE,
    val gameMode: GameMode = GameMode.PVP,
    val aiDifficulty: AIDifficulty = AIDifficulty.NORMAL,
    val aiColor: com.gogame.game.StoneColor = com.gogame.game.StoneColor.WHITE,
    val showCoordinates: Boolean = true,
    val showMoveNumbers: Boolean = false,
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val komi: Double = 7.5
)
