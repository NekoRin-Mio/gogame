package com.gogame.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gogame.data.AIDifficulty
import com.gogame.data.BoardSize
import com.gogame.data.GameMode
import com.gogame.data.GameSettings
import com.gogame.game.GoAI
import com.gogame.game.GoEngine
import com.gogame.game.GameScore
import com.gogame.game.MoveResult
import com.gogame.game.Position
import com.gogame.game.StoneColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 游戏状态 UI 模型
 */
data class GameUiState(
    val boardSize: Int = 19,
    val board: Array<Array<StoneColor>> = Array(19) { Array(19) { StoneColor.NONE } },
    val currentPlayer: StoneColor = StoneColor.BLACK,
    val blackCaptures: Int = 0,
    val whiteCaptures: Int = 0,
    val lastMove: Position? = null,
    val koPoint: Position? = null,
    val isGameOver: Boolean = false,
    val moveNumber: Int = 0,
    val score: GameScore? = null,
    val message: String? = null,
    val isAiThinking: Boolean = false,
    val settings: GameSettings = GameSettings(),
    val capturedStones: List<Position> = emptyList()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GameUiState) return false
        return boardSize == other.boardSize &&
                board.contentDeepEquals(other.board) &&
                currentPlayer == other.currentPlayer &&
                blackCaptures == other.blackCaptures &&
                whiteCaptures == other.whiteCaptures &&
                lastMove == other.lastMove &&
                isGameOver == other.isGameOver &&
                moveNumber == other.moveNumber
    }

    override fun hashCode(): Int {
        var result = boardSize
        result = 31 * result + board.contentDeepHashCode()
        result = 31 * result + currentPlayer.hashCode()
        result = 31 * result + blackCaptures
        result = 31 * result + whiteCaptures
        result = 31 * result + (lastMove?.hashCode() ?: 0)
        result = 31 * result + isGameOver.hashCode()
        result = 31 * result + moveNumber
        return result
    }
}

class GameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var engine: GoEngine = GoEngine(_uiState.value.settings.boardSize.size)
    private var ai: GoAI = GoAI(_uiState.value.settings.boardSize.size)
    private var settings: GameSettings = GameSettings()

    init {
        updateUiState()
    }

    /**
     * 落子
     */
    fun playMove(x: Int, y: Int) {
        if (_uiState.value.isGameOver || _uiState.value.isAiThinking) return
        if (_uiState.value.settings.gameMode == GameMode.PVE &&
            _uiState.value.currentPlayer == settings.aiColor
        ) return

        val result = engine.playMove(x, y)
        when (result) {
            is MoveResult.Success -> {
                updateUiState(capturedStones = result.capturedStones)
                if (result.capturedStones.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(message = "提子 ${result.capturedStones.size} 颗！")
                } else {
                    _uiState.value = _uiState.value.copy(message = null)
                }
                maybeTriggerAI()
            }
            is MoveResult.IllegalMove -> {
                _uiState.value = _uiState.value.copy(message = "禁着点！不可自杀")
            }
            is MoveResult.KoViolation -> {
                _uiState.value = _uiState.value.copy(message = "劫争禁着！请先在他处落子")
            }
            is MoveResult.Occupied -> {
                _uiState.value = _uiState.value.copy(message = "此处已有棋子")
            }
        }
    }

    /**
     * 虚手
     */
    fun pass() {
        if (_uiState.value.isGameOver || _uiState.value.isAiThinking) return
        engine.pass()
        if (engine.getState().isGameOver) {
            val score = engine.calculateScore()
            updateUiState(score = score, isGameOver = true)
        } else {
            updateUiState(message = "虚手（Pass）")
            maybeTriggerAI()
        }
    }

    /**
     * 悔棋
     */
    fun undo() {
        if (_uiState.value.isAiThinking) return

        // 人机模式下悔两步（AI的+自己的）
        val stepsToUndo = if (settings.gameMode == GameMode.PVE) 2 else 1
        for (i in 0 until stepsToUndo) {
            engine.undo()
        }
        updateUiState(message = "悔棋")
    }

    /**
     * 重新开始
     */
    fun newGame(newSettings: GameSettings = settings) {
        settings = newSettings
        engine = GoEngine(newSettings.boardSize.size)
        ai = GoAI(newSettings.boardSize.size)
        _uiState.value = GameUiState(settings = newSettings)
        updateUiState()

        // 如果AI执黑，先走
        if (newSettings.gameMode == GameMode.PVE && newSettings.aiColor == StoneColor.BLACK) {
            maybeTriggerAI()
        }
    }

    /**
     * 更新设置
     */
    fun updateSettings(newSettings: GameSettings) {
        settings = newSettings
        _uiState.value = _uiState.value.copy(settings = newSettings)
    }

    /**
     * 数子计分
     */
    fun calculateFinalScore() {
        val score = engine.calculateScore()
        updateUiState(score = score, isGameOver = true)
    }

    private fun maybeTriggerAI() {
        if (settings.gameMode != GameMode.PVE) return
        if (_uiState.value.isGameOver) return
        if (_uiState.value.currentPlayer != settings.aiColor) return

        _uiState.value = _uiState.value.copy(isAiThinking = true)

        viewModelScope.launch(Dispatchers.Default) {
            val aiMove = ai.findBestMove(engine, settings.aiColor)
            if (aiMove != null) {
                engine.playMove(aiMove.x, aiMove.y)
            } else {
                engine.pass()
            }

            _uiState.value = _uiState.value.copy(isAiThinking = false)
            if (engine.getState().isGameOver) {
                val score = engine.calculateScore()
                updateUiState(score = score, isGameOver = true)
            } else {
                updateUiState(message = if (aiMove == null) "AI 虚手" else null)
            }
        }
    }

    private fun updateUiState(
        message: String? = null,
        score: GameScore? = null,
        isGameOver: Boolean = false,
        capturedStones: List<Position> = emptyList()
    ) {
        val state = engine.getState()
        _uiState.value = _uiState.value.copy(
            boardSize = state.boardSize,
            board = state.board.map { it.copyOf() }.toTypedArray(),
            currentPlayer = state.currentPlayer,
            blackCaptures = state.blackCaptures,
            whiteCaptures = state.whiteCaptures,
            lastMove = state.lastMove,
            koPoint = state.koPoint,
            isGameOver = isGameOver || state.isGameOver,
            moveNumber = state.moveHistory.size,
            score = score,
            message = message,
            capturedStones = capturedStones,
            settings = settings
        )
    }
}
