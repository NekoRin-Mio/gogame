package com.gogame.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.gogame.game.Position
import com.gogame.game.StoneColor

/**
 * 围棋棋盘组件
 */
@Composable
fun GoBoard(
    boardSize: Int,
    board: Array<Array<StoneColor>>,
    lastMove: Position?,
    koPoint: Position?,
    showCoordinates: Boolean,
    showMoveNumbers: Boolean,
    moveNumber: Int,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() > 0.5f
    val woodColor = if (isDark) BoardWoodColor else BoardWoodColor
    val woodDarkColor = if (isDark) BoardWoodDarkColor else BoardWoodDarkColor
    val lineColor = if (isDark) BoardLineColor else BoardLineColor
    val starColor = lineColor

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .padding(if (showCoordinates) 12.dp else 4.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(woodColor, woodDarkColor)
                    )
                )
                .pointerInput(boardSize, showCoordinates) {
                    detectTapGestures { offset ->
                        val margin = if (showCoordinates) 32f else 8f
                        val totalSize = size.width - margin * 2
                        val cellSize = if (boardSize > 1) totalSize / (boardSize - 1) else totalSize

                        val x = ((offset.x - margin) / cellSize).toInt()
                        val y = ((offset.y - margin) / cellSize).toInt()

                        if (x in 0 until boardSize && y in 0 until boardSize) {
                            onCellClick(x, y)
                        }
                    }
                }
        ) {
            drawBoard(
                boardSize = boardSize,
                board = board,
                lastMove = lastMove,
                koPoint = koPoint,
                showCoordinates = showCoordinates,
                lineColor = lineColor,
                starColor = starColor
            )
        }
    }
}

private fun DrawScope.drawBoard(
    boardSize: Int,
    board: Array<Array<StoneColor>>,
    lastMove: Position?,
    koPoint: Position?,
    showCoordinates: Boolean,
    lineColor: Color,
    starColor: Color
) {
    val margin = if (showCoordinates) 32.dp.toPx() else 8.dp.toPx()
    val totalSize = size.width - margin * 2
    val cellSize = if (boardSize > 1) totalSize / (boardSize - 1) else totalSize

    // 绘制坐标
    if (showCoordinates) {
        drawCoordinates(boardSize, margin, cellSize, totalSize, lineColor)
    }

    // 绘制棋盘线
    for (i in 0 until boardSize) {
        val pos = margin + i * cellSize
        drawLine(
            color = lineColor,
            start = Offset(margin, pos),
            end = Offset(margin + totalSize, pos),
            strokeWidth = if (i == 0 || i == boardSize - 1) 2.dp.toPx() else 1.dp.toPx()
        )
        drawLine(
            color = lineColor,
            start = Offset(pos, margin),
            end = Offset(pos, margin + totalSize),
            strokeWidth = if (i == 0 || i == boardSize - 1) 2.dp.toPx() else 1.dp.toPx()
        )
    }

    // 绘制星位
    val starPoints = getStarPoints(boardSize)
    for (star in starPoints) {
        drawCircle(
            color = starColor,
            radius = 3.dp.toPx(),
            center = Offset(margin + star.x * cellSize, margin + star.y * cellSize)
        )
    }

    // 绘制棋子
    for (x in 0 until boardSize) {
        for (y in 0 until boardSize) {
            val stone = board[x][y]
            if (stone != StoneColor.NONE) {
                drawStone(
                    centerX = margin + x * cellSize,
                    centerY = margin + y * cellSize,
                    radius = cellSize * 0.46f,
                    color = stone
                )
            }
        }
    }

    // 绘制最后一手标记
    if (lastMove != null) {
        val stone = board[lastMove.x][lastMove.y]
        if (stone != StoneColor.NONE) {
            val markerColor = if (stone == StoneColor.BLACK) Color(0xFFE0E0E0) else Color(0xFF333333)
            drawCircle(
                color = markerColor,
                radius = cellSize * 0.15f,
                center = Offset(margin + lastMove.x * cellSize, margin + lastMove.y * cellSize)
            )
        }
    }

    // 绘制劫争禁着点
    if (koPoint != null) {
        drawCircle(
            color = Color(0xFFE53935),
            radius = cellSize * 0.15f,
            center = Offset(margin + koPoint.x * cellSize, margin + koPoint.y * cellSize),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

private fun DrawScope.drawCoordinates(
    boardSize: Int,
    margin: Float,
    cellSize: Float,
    totalSize: Float,
    textColor: Color
) {
    val letters = "ABCDEFGHJKLMNOPQRST"
    val paint = Paint().apply {
        textSize = 9.dp.toPx()
        this.color = android.graphics.Color.argb(
            (textColor.alpha * 255).toInt(),
            (textColor.red * 255).toInt(),
            (textColor.green * 255).toInt(),
            (textColor.blue * 255).toInt()
        )
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    for (i in 0 until boardSize) {
        val pos = margin + i * cellSize
        val letter = letters.substring(i, i + 1)
        val numStr = (boardSize - i).toString()

        // 上方字母
        drawContext.canvas.nativeCanvas.drawText(
            letter, pos, margin / 2 + paint.textSize / 3, paint
        )
        // 下方字母
        drawContext.canvas.nativeCanvas.drawText(
            letter, pos, margin + totalSize + margin / 2 + paint.textSize / 3, paint
        )
        // 左侧数字
        drawContext.canvas.nativeCanvas.drawText(
            numStr, margin / 2, pos + paint.textSize / 3, paint
        )
        // 右侧数字
        drawContext.canvas.nativeCanvas.drawText(
            numStr, margin + totalSize + margin / 2, pos + paint.textSize / 3, paint
        )
    }
}

private fun DrawScope.drawStone(
    centerX: Float,
    centerY: Float,
    radius: Float,
    color: StoneColor
) {
    when (color) {
        StoneColor.BLACK -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF5A5A5A), BlackStoneColor),
                    center = Offset(centerX - radius * 0.3f, centerY - radius * 0.3f),
                    radius = radius * 1.5f
                ),
                radius = radius,
                center = Offset(centerX, centerY)
            )
            // 高光
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF999999).copy(alpha = 0.5f), Color.Transparent),
                    center = Offset(centerX - radius * 0.35f, centerY - radius * 0.35f),
                    radius = radius * 0.5f
                ),
                radius = radius * 0.5f,
                center = Offset(centerX - radius * 0.3f, centerY - radius * 0.3f)
            )
        }
        StoneColor.WHITE -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFFFFF), WhiteStoneColor, WhiteStoneBorderColor),
                    center = Offset(centerX - radius * 0.3f, centerY - radius * 0.3f),
                    radius = radius * 1.5f
                ),
                radius = radius,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = WhiteStoneBorderColor.copy(alpha = 0.4f),
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 0.5.dp.toPx())
            )
        }
        StoneColor.NONE -> {}
    }
}

private fun getStarPoints(boardSize: Int): List<Position> {
    return when (boardSize) {
        9 -> listOf(
            Position(2, 2), Position(6, 2),
            Position(2, 6), Position(6, 6),
            Position(4, 4)
        )
        13 -> listOf(
            Position(3, 3), Position(6, 3), Position(9, 3),
            Position(3, 6), Position(6, 6), Position(9, 6),
            Position(3, 9), Position(6, 9), Position(9, 9)
        )
        19 -> listOf(
            Position(3, 3), Position(9, 3), Position(15, 3),
            Position(3, 9), Position(9, 9), Position(15, 9),
            Position(3, 15), Position(9, 15), Position(15, 15)
        )
        else -> emptyList()
    }
}

fun Color.luminance(): Float {
    return 0.299f * red + 0.587f * green + 0.114f * blue
}
