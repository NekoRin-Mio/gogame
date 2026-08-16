package com.gogame.game

/**
 * 棋子颜色
 */
enum class StoneColor {
    NONE,   // 空位
    BLACK,  // 黑子
    WHITE   // 白子
}

/**
 * 棋盘位置
 */
data class Position(val x: Int, val y: Int) {
    fun isValid(boardSize: Int): Boolean {
        return x in 0 until boardSize && y in 0 until boardSize
    }
}

/**
 * 落子结果
 */
sealed class MoveResult {
    data class Success(val capturedStones: List<Position>) : MoveResult()
    data object IllegalMove : MoveResult()        // 禁着点（自杀）
    data object KoViolation : MoveResult()        // 劫争禁着
    data object Occupied : MoveResult()           // 已有棋子
}

/**
 * 游戏状态
 */
data class GameState(
    val boardSize: Int = 19,
    val board: Array<Array<StoneColor>> = Array(19) { Array(19) { StoneColor.NONE } },
    val currentPlayer: StoneColor = StoneColor.BLACK,
    val blackCaptures: Int = 0,
    val whiteCaptures: Int = 0,
    val moveHistory: MutableList<Move> = mutableListOf(),
    val lastMove: Position? = null,
    val koPoint: Position? = null,
    val passCount: Int = 0,
    val isGameOver: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GameState) return false
        return boardSize == other.boardSize &&
                board.contentDeepEquals(other.board) &&
                currentPlayer == other.currentPlayer &&
                blackCaptures == other.blackCaptures &&
                whiteCaptures == other.whiteCaptures
    }

    override fun hashCode(): Int {
        var result = boardSize
        result = 31 * result + board.contentDeepHashCode()
        result = 31 * result + currentPlayer.hashCode()
        result = 31 * result + blackCaptures
        result = 31 * result + whiteCaptures
        return result
    }

    fun copy(): GameState {
        return GameState(
            boardSize = boardSize,
            board = board.map { it.copyOf() }.toTypedArray(),
            currentPlayer = currentPlayer,
            blackCaptures = blackCaptures,
            whiteCaptures = whiteCaptures,
            moveHistory = moveHistory.toMutableList(),
            lastMove = lastMove,
            koPoint = koPoint,
            passCount = passCount,
            isGameOver = isGameOver
        )
    }
}

/**
 * 一步棋
 */
data class Move(
    val position: Position?,
    val color: StoneColor,
    val capturedStones: List<Position> = emptyList(),
    val isPass: Boolean = false
)
