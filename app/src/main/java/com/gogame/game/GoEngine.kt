package com.gogame.game

/**
 * 围棋核心规则引擎
 * 实现：落子、提子、禁着点检测、劫争检测、数子计分
 */
class GoEngine(private val boardSize: Int = 19) {

    private var state: GameState = GameState(boardSize = boardSize)

    fun getState(): GameState = state

    fun reset(newSize: Int = boardSize) {
        state = GameState(boardSize = newSize)
    }

    /**
     * 尝试落子
     */
    fun playMove(x: Int, y: Int): MoveResult {
        val pos = Position(x, y)
        if (!pos.isValid(state.boardSize)) return MoveResult.IllegalMove
        if (state.isGameOver) return MoveResult.IllegalMove

        // 检查是否已有棋子
        if (state.board[x][y] != StoneColor.NONE) return MoveResult.Occupied

        // 检查劫争禁着点
        if (state.koPoint != null && state.koPoint == pos) return MoveResult.KoViolation

        val color = state.currentPlayer
        val opponent = if (color == StoneColor.BLACK) StoneColor.WHITE else StoneColor.BLACK

        // 在棋盘上试放棋子
        state.board[x][y] = color

        // 检查并提取对方无气棋子
        val capturedStones = mutableListOf<Position>()
        for (neighbor in getNeighbors(pos)) {
            if (state.board[neighbor.x][neighbor.y] == opponent) {
                val group = findGroup(neighbor)
                if (countLiberties(group) == 0) {
                    capturedStones.addAll(group)
                }
            }
        }

        // 移除被提的棋子
        for (captured in capturedStones) {
            state.board[captured.x][captured.y] = StoneColor.NONE
        }

        // 检查自杀规则
        val selfGroup = findGroup(pos)
        if (countLiberties(selfGroup) == 0 && capturedStones.isEmpty()) {
            // 自杀，回退
            state.board[x][y] = StoneColor.NONE
            return MoveResult.IllegalMove
        }

        // 检查劫争：如果只提了一个子，且新落的子也只有一气
        var newKoPoint: Position? = null
        if (capturedStones.size == 1 && selfGroup.size == 1 && countLiberties(selfGroup) == 1) {
            newKoPoint = capturedStones[0]
        }

        // 更新状态
        val move = Move(pos, color, capturedStones)
        state.moveHistory.add(move)

        state = state.copy(
            currentPlayer = opponent,
            blackCaptures = if (color == StoneColor.BLACK) state.blackCaptures + capturedStones.size else state.blackCaptures,
            whiteCaptures = if (color == StoneColor.WHITE) state.whiteCaptures + capturedStones.size else state.whiteCaptures,
            lastMove = pos,
            koPoint = newKoPoint,
            passCount = 0
        )

        return MoveResult.Success(capturedStones)
    }

    /**
     * 虚手（pass）
     */
    fun pass() {
        val opponent = if (state.currentPlayer == StoneColor.BLACK) StoneColor.WHITE else StoneColor.BLACK
        state.moveHistory.add(Move(null, state.currentPlayer, isPass = true))
        state = state.copy(
            currentPlayer = opponent,
            passCount = state.passCount + 1,
            lastMove = null,
            koPoint = null,
            isGameOver = state.passCount + 1 >= 2
        )
    }

    /**
     * 悔棋
     */
    fun undo(): Boolean {
        if (state.moveHistory.isEmpty()) return false

        // 移除最后一步
        state.moveHistory.removeAt(state.moveHistory.size - 1)

        // 保存历史并重建棋盘状态
        val history = state.moveHistory.toList()
        state = GameState(boardSize = state.boardSize)
        for (move in history) {
            if (move.isPass) {
                pass()
            } else {
                playMove(move.position!!.x, move.position.y)
            }
        }

        return true
    }

    /**
     * 数子计分（简化版中国规则）
     */
    fun calculateScore(): GameScore {
        val territory = countTerritory()
        var blackScore: Double = (territory.blackTerritory + state.blackCaptures).toDouble()
        var whiteScore: Double = (territory.whiteTerritory + state.whiteCaptures).toDouble()

        // 贴目（中国规则通常贴3又3/4子，即7.5目）
        val komi = 7.5
        whiteScore += komi

        return GameScore(
            blackTerritory = territory.blackTerritory,
            whiteTerritory = territory.whiteTerritory,
            blackCaptures = state.blackCaptures,
            whiteCaptures = state.whiteCaptures,
            komi = komi,
            blackTotal = blackScore,
            whiteTotal = whiteScore,
            winner = if (blackScore > whiteScore) StoneColor.BLACK else StoneColor.WHITE,
            scoreDiff = kotlin.math.abs(blackScore - whiteScore)
        )
    }

    // === 私有方法 ===

    private fun getNeighbors(pos: Position): List<Position> {
        val neighbors = mutableListOf<Position>()
        val dx = intArrayOf(0, 0, 1, -1)
        val dy = intArrayOf(1, -1, 0, 0)
        for (i in 0 until 4) {
            val nx = pos.x + dx[i]
            val ny = pos.y + dy[i]
            if (nx in 0 until state.boardSize && ny in 0 until state.boardSize) {
                neighbors.add(Position(nx, ny))
            }
        }
        return neighbors
    }

    private fun findGroup(start: Position): List<Position> {
        val color = state.board[start.x][start.y]
        if (color == StoneColor.NONE) return emptyList()

        val group = mutableListOf<Position>()
        val visited = mutableSetOf<Position>()
        val queue = ArrayDeque<Position>()
        queue.add(start)
        visited.add(start)

        while (queue.isNotEmpty()) {
            val pos = queue.removeFirst()
            group.add(pos)
            for (neighbor in getNeighbors(pos)) {
                if (neighbor !in visited && state.board[neighbor.x][neighbor.y] == color) {
                    visited.add(neighbor)
                    queue.add(neighbor)
                }
            }
        }
        return group
    }

    private fun countLiberties(group: List<Position>): Int {
        val liberties = mutableSetOf<Position>()
        for (pos in group) {
            for (neighbor in getNeighbors(pos)) {
                if (state.board[neighbor.x][neighbor.y] == StoneColor.NONE) {
                    liberties.add(neighbor)
                }
            }
        }
        return liberties.size
    }

    private fun countTerritory(): TerritoryResult {
        val visited = Array(state.boardSize) { Array(state.boardSize) { false } }
        var blackTerritory = 0
        var whiteTerritory = 0

        for (x in 0 until state.boardSize) {
            for (y in 0 until state.boardSize) {
                if (state.board[x][y] == StoneColor.NONE && !visited[x][y]) {
                    val (size, owner) = floodFillTerritory(x, y, visited)
                    when (owner) {
                        StoneColor.BLACK -> blackTerritory += size
                        StoneColor.WHITE -> whiteTerritory += size
                        else -> {}
                    }
                }
            }
        }
        return TerritoryResult(blackTerritory, whiteTerritory)
    }

    private fun floodFillTerritory(x: Int, y: Int, visited: Array<Array<Boolean>>): Pair<Int, StoneColor> {
        val queue = ArrayDeque<Position>()
        queue.add(Position(x, y))
        visited[x][y] = true
        var size = 0
        var touchesBlack = false
        var touchesWhite = false

        while (queue.isNotEmpty()) {
            val pos = queue.removeFirst()
            size++

            for (neighbor in getNeighbors(pos)) {
                when (state.board[neighbor.x][neighbor.y]) {
                    StoneColor.BLACK -> touchesBlack = true
                    StoneColor.WHITE -> touchesWhite = true
                    StoneColor.NONE -> {
                        if (!visited[neighbor.x][neighbor.y]) {
                            visited[neighbor.x][neighbor.y] = true
                            queue.add(neighbor)
                        }
                    }
                }
            }
        }

        val owner = when {
            touchesBlack && !touchesWhite -> StoneColor.BLACK
            touchesWhite && !touchesBlack -> StoneColor.WHITE
            else -> StoneColor.NONE
        }
        return Pair(size, owner)
    }
}

/**
 * 领地统计结果
 */
data class TerritoryResult(
    val blackTerritory: Int,
    val whiteTerritory: Int
)

/**
 * 最终得分
 */
data class GameScore(
    val blackTerritory: Int,
    val whiteTerritory: Int,
    val blackCaptures: Int,
    val whiteCaptures: Int,
    val komi: Double,
    val blackTotal: Double,
    val whiteTotal: Double,
    val winner: StoneColor,
    val scoreDiff: Double
)
