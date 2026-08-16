package com.gogame.game

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * 简单 AI 对手
 * 策略：启发式评估 + 随机性
 */
class GoAI(private val boardSize: Int = 19) {

    /**
     * 根据当前棋盘状态选择最佳落子位置
     */
    fun findBestMove(engine: GoEngine, aiColor: StoneColor): Position? {
        val state = engine.getState()
        val candidates = mutableListOf<Pair<Position, Int>>()

        // 遍历所有合法空位
        for (x in 0 until boardSize) {
            for (y in 0 until boardSize) {
                if (state.board[x][y] != StoneColor.NONE) continue

                val pos = Position(x, y)
                val score = evaluateMove(state, pos, aiColor)

                if (score > 0) {
                    candidates.add(Pair(pos, score))
                }
            }
        }

        if (candidates.isEmpty()) return null

        // 取分数最高的几个位置，加入随机性
        candidates.sortByDescending { it.second }
        val topN = min(candidates.size, max(3, candidates.size / 5))
        val topMoves = candidates.subList(0, topN)
        return topMoves.random().first
    }

    /**
     * 评估某个位置的落子价值
     */
    private fun evaluateMove(state: GameState, pos: Position, color: StoneColor): Int {
        var score = 0
        val opponent = if (color == StoneColor.BLACK) StoneColor.WHITE else StoneColor.BLACK

        // 检查能否提子
        val tempBoard = state.board.map { it.copyOf() }.toTypedArray()
        tempBoard[pos.x][pos.y] = color

        var captureCount = 0
        for (neighbor in getNeighbors(pos, boardSize)) {
            if (tempBoard[neighbor.x][neighbor.y] == opponent) {
                val group = findGroupOnBoard(tempBoard, neighbor, boardSize)
                if (countLibertiesOnBoard(tempBoard, group, boardSize) == 0) {
                    captureCount += group.size
                }
            }
        }
        score += captureCount * 100

        // 检查自己的气数
        tempBoard[pos.x][pos.y] = color
        // 先移除被提的子
        for (neighbor in getNeighbors(pos, boardSize)) {
            if (tempBoard[neighbor.x][neighbor.y] == opponent) {
                val group = findGroupOnBoard(tempBoard, neighbor, boardSize)
                if (countLibertiesOnBoard(tempBoard, group, boardSize) == 0) {
                    for (g in group) tempBoard[g.x][g.y] = StoneColor.NONE
                }
            }
        }
        val selfGroup = findGroupOnBoard(tempBoard, pos, boardSize)
        val selfLiberties = countLibertiesOnBoard(tempBoard, selfGroup, boardSize)

        if (selfLiberties == 0) return 0 // 自杀，不选

        // 气数越多越好
        score += selfLiberties * 10

        // 如果自己只有一气，扣分（容易被提）
        if (selfLiberties == 1) score -= 50

        // 评估对对方棋子的影响
        for (neighbor in getNeighbors(pos, boardSize)) {
            if (state.board[neighbor.x][neighbor.y] == opponent) {
                val oppGroup = findGroupOnBoard(state.board, neighbor, boardSize)
                val oppLiberties = countLibertiesOnBoard(state.board, oppGroup, boardSize)
                when {
                    oppLiberties == 1 -> score += 80  // 打吃
                    oppLiberties == 2 -> score += 30  // 紧气
                }
            }
            if (state.board[neighbor.x][neighbor.y] == color) {
                // 连接己方棋子
                score += 5
            }
        }

        // 位置评分：中心和星位附近更好
        val center = boardSize / 2
        val distToCenter = abs(pos.x - center) + abs(pos.y - center)
        score += max(0, boardSize - distToCenter)

        // 不要太靠近边缘
        val edgeDist = min(min(pos.x, pos.y), min(boardSize - 1 - pos.x, boardSize - 1 - pos.y))
        if (edgeDist == 0) score -= 20
        if (edgeDist == 1) score -= 10

        // 增加随机性
        score += (0..15).random()

        return score
    }

    private fun getNeighbors(pos: Position, size: Int): List<Position> {
        val neighbors = mutableListOf<Position>()
        val dx = intArrayOf(0, 0, 1, -1)
        val dy = intArrayOf(1, -1, 0, 0)
        for (i in 0 until 4) {
            val nx = pos.x + dx[i]
            val ny = pos.y + dy[i]
            if (nx in 0 until size && ny in 0 until size) {
                neighbors.add(Position(nx, ny))
            }
        }
        return neighbors
    }

    private fun findGroupOnBoard(board: Array<Array<StoneColor>>, start: Position, size: Int): List<Position> {
        val color = board[start.x][start.y]
        if (color == StoneColor.NONE) return emptyList()

        val group = mutableListOf<Position>()
        val visited = mutableSetOf<Position>()
        val queue = ArrayDeque<Position>()
        queue.add(start)
        visited.add(start)

        while (queue.isNotEmpty()) {
            val pos = queue.removeFirst()
            group.add(pos)
            for (neighbor in getNeighbors(pos, size)) {
                if (neighbor !in visited && board[neighbor.x][neighbor.y] == color) {
                    visited.add(neighbor)
                    queue.add(neighbor)
                }
            }
        }
        return group
    }

    private fun countLibertiesOnBoard(board: Array<Array<StoneColor>>, group: List<Position>, size: Int): Int {
        val liberties = mutableSetOf<Position>()
        for (pos in group) {
            for (neighbor in getNeighbors(pos, size)) {
                if (board[neighbor.x][neighbor.y] == StoneColor.NONE) {
                    liberties.add(neighbor)
                }
            }
        }
        return liberties.size
    }
}
