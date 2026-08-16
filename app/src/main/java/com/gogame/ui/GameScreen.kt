package com.gogame.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gogame.game.GameScore
import com.gogame.game.StoneColor

/**
 * 游戏主界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showScoreDialog by remember { mutableStateOf(false) }
    var showNewGameDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "围棋",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 玩家信息栏
            PlayerInfoBar(uiState)

            Spacer(modifier = Modifier.height(8.dp))

            // 棋盘
            GoBoard(
                boardSize = uiState.boardSize,
                board = uiState.board,
                lastMove = uiState.lastMove,
                koPoint = uiState.koPoint,
                showCoordinates = uiState.settings.showCoordinates,
                showMoveNumbers = uiState.settings.showMoveNumbers,
                moveNumber = uiState.moveNumber,
                onCellClick = { x, y -> viewModel.playMove(x, y) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 消息提示
            AnimatedVisibility(
                visible = uiState.message != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                uiState.message?.let { msg ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Text(
                            text = msg,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // AI 思考中指示器
            AnimatedVisibility(visible = uiState.isAiThinking) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI 思考中...", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 操作按钮栏
            ActionButtonsRow(
                uiState = uiState,
                onPass = { viewModel.pass() },
                onUndo = { viewModel.undo() },
                onScore = {
                    viewModel.calculateFinalScore()
                    showScoreDialog = true
                },
                onNewGame = { showNewGameDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // 设置对话框
    if (showSettingsDialog) {
        SettingsDialog(
            currentSettings = uiState.settings,
            onDismiss = { showSettingsDialog = false },
            onConfirm = { newSettings ->
                viewModel.updateSettings(newSettings)
                showSettingsDialog = false
            }
        )
    }

    // 数子对话框
    val currentScore = uiState.score
    if (showScoreDialog && currentScore != null) {
        ScoreDialog(
            score = currentScore,
            onDismiss = { showScoreDialog = false }
        )
    }

    // 新游戏对话框
    if (showNewGameDialog) {
        NewGameDialog(
            currentSettings = uiState.settings,
            onDismiss = { showNewGameDialog = false },
            onConfirm = { newSettings ->
                viewModel.newGame(newSettings)
                showNewGameDialog = false
            }
        )
    }
}

/**
 * 玩家信息栏
 */
@Composable
private fun PlayerInfoBar(uiState: GameUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PlayerCard(
            color = StoneColor.BLACK,
            captures = uiState.blackCaptures,
            isCurrentPlayer = uiState.currentPlayer == StoneColor.BLACK && !uiState.isGameOver,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        PlayerCard(
            color = StoneColor.WHITE,
            captures = uiState.whiteCaptures,
            isCurrentPlayer = uiState.currentPlayer == StoneColor.WHITE && !uiState.isGameOver,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PlayerCard(
    color: StoneColor,
    captures: Int,
    isCurrentPlayer: Boolean,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isCurrentPlayer)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceContainerHigh

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentPlayer) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 棋子图标
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (color == StoneColor.BLACK) BlackStoneColor else WhiteStoneColor
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = if (color == StoneColor.BLACK) "黑方" else "白方",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isCurrentPlayer)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "提子: $captures",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCurrentPlayer)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 操作按钮栏
 */
@Composable
private fun ActionButtonsRow(
    uiState: GameUiState,
    onPass: () -> Unit,
    onUndo: () -> Unit,
    onScore: () -> Unit,
    onNewGame: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionButton(
            icon = Icons.AutoMirrored.Filled.Undo,
            text = "悔棋",
            enabled = !uiState.isAiThinking,
            onClick = onUndo
        )
        ActionButton(
            icon = Icons.Default.SkipNext,
            text = "虚手",
            enabled = !uiState.isAiThinking && !uiState.isGameOver,
            onClick = onPass
        )
        ActionButton(
            icon = Icons.Default.Calculate,
            text = "数子",
            enabled = !uiState.isAiThinking,
            onClick = onScore
        )
        ActionButton(
            icon = Icons.Default.Refresh,
            text = "新局",
            enabled = true,
            onClick = onNewGame
        )
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilledTonalIconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(icon, contentDescription = text, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

/**
 * 数子结果对话框
 */
@Composable
private fun ScoreDialog(
    score: GameScore,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("对局结果", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                // 胜利者
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (score.winner == StoneColor.BLACK) "黑方胜" else "白方胜",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "差距 ${String.format("%.1f", score.scoreDiff)} 子",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 详细得分
                ScoreRow("黑方领地", score.blackTerritory.toString())
                ScoreRow("黑方提子", score.blackCaptures.toString())
                DividerRow()
                ScoreRow("白方领地", score.whiteTerritory.toString())
                ScoreRow("白方提子", score.whiteCaptures.toString())
                ScoreRow("贴目", String.format("%.1f", score.komi))
                DividerRow()
                ScoreRow("黑方总分", String.format("%.1f", score.blackTotal), bold = true)
                ScoreRow("白方总分", String.format("%.1f", score.whiteTotal), bold = true)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}

@Composable
private fun ScoreRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (bold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = if (bold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun DividerRow() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

/**
 * 新游戏对话框
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun NewGameDialog(
    currentSettings: com.gogame.data.GameSettings,
    onDismiss: () -> Unit,
    onConfirm: (com.gogame.data.GameSettings) -> Unit
) {
    var settings by remember { mutableStateOf(currentSettings) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("开始新对局", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("棋盘大小", style = MaterialTheme.typography.titleSmall)
                com.gogame.data.BoardSize.entries.forEach { size ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = settings.boardSize == size,
                            onClick = { settings = settings.copy(boardSize = size) }
                        )
                        Text(size.displayName)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("游戏模式", style = MaterialTheme.typography.titleSmall)
                com.gogame.data.GameMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = settings.gameMode == mode,
                            onClick = { settings = settings.copy(gameMode = mode) }
                        )
                        Text(if (mode == com.gogame.data.GameMode.PVP) "双人对弈" else "人机对弈")
                    }
                }

                if (settings.gameMode == com.gogame.data.GameMode.PVE) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("AI 执子", style = MaterialTheme.typography.titleSmall)
                    Row {
                        FilterChip(
                            selected = settings.aiColor == StoneColor.WHITE,
                            onClick = { settings = settings.copy(aiColor = StoneColor.WHITE) },
                            label = { Text("白方（后手）") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = settings.aiColor == StoneColor.BLACK,
                            onClick = { settings = settings.copy(aiColor = StoneColor.BLACK) },
                            label = { Text("黑方（先手）") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(settings) }) {
                Text("开始")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 设置对话框
 */
@Composable
private fun SettingsDialog(
    currentSettings: com.gogame.data.GameSettings,
    onDismiss: () -> Unit,
    onConfirm: (com.gogame.data.GameSettings) -> Unit
) {
    var settings by remember { mutableStateOf(currentSettings) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("AI 难度", style = MaterialTheme.typography.titleSmall)
                com.gogame.data.AIDifficulty.entries.forEach { difficulty ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = settings.aiDifficulty == difficulty,
                            onClick = { settings = settings.copy(aiDifficulty = difficulty) }
                        )
                        Text(difficulty.displayName)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                SwitchRow(
                    label = "显示坐标",
                    checked = settings.showCoordinates,
                    onCheckedChange = { settings = settings.copy(showCoordinates = it) }
                )
                SwitchRow(
                    label = "显示步数",
                    checked = settings.showMoveNumbers,
                    onCheckedChange = { settings = settings.copy(showMoveNumbers = it) }
                )
                SwitchRow(
                    label = "音效",
                    checked = settings.soundEnabled,
                    onCheckedChange = { settings = settings.copy(soundEnabled = it) }
                )
                SwitchRow(
                    label = "震动反馈",
                    checked = settings.hapticEnabled,
                    onCheckedChange = { settings = settings.copy(hapticEnabled = it) }
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("贴目: ${String.format("%.1f", settings.komi)}", style = MaterialTheme.typography.titleSmall)
                Slider(
                    value = settings.komi.toFloat(),
                    onValueChange = { settings = settings.copy(komi = it.toDouble()) },
                    valueRange = 0f..10f,
                    steps = 19
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(settings) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
