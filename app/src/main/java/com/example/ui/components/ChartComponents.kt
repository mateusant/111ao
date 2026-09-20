package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SecAmber
import com.example.ui.theme.SecCyanLight
import com.example.ui.theme.SecCyanPrimary
import com.example.ui.theme.SecEmerald
import com.example.ui.theme.SecRed
import java.util.Locale

@Composable
fun TacticalBarChart(
    title: String,
    data: List<Pair<String, Int>>,
    accentColor: Color = SecCyanPrimary,
    modifier: Modifier = Modifier
) {
    val maxVal = (data.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val canvasW = size.width
                val canvasH = size.height - 25f // leave room for labels
                val barCount = data.size
                if (barCount == 0) return@Canvas

                val barWidth = (canvasW / (barCount * 1.5f)).coerceAtMost(36f)
                val spacing = (canvasW - (barWidth * barCount)) / (barCount + 1)

                // Grid lines
                for (i in 1..3) {
                    val y = canvasH * (1f - i / 3f)
                    drawLine(
                        color = Color(0xFF334155).copy(alpha = 0.3f),
                        start = Offset(0f, y),
                        end = Offset(canvasW, y),
                        strokeWidth = 1f
                    )
                }

                data.forEachIndexed { index, pair ->
                    val x = spacing + index * (barWidth + spacing)
                    val barHeight = (pair.second.toFloat() / maxVal) * canvasH
                    val y = canvasH - barHeight

                    // Bar with gradient
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(accentColor, accentColor.copy(alpha = 0.6f)),
                            startY = y,
                            endY = canvasH
                        ),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )

                    // Value label on top of bar
                    drawContext.canvas.nativeCanvas.apply {
                        val valPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 18f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                        }
                        drawText("${pair.second}", x + barWidth / 2, (y - 6f).coerceAtLeast(16f), valPaint)

                        // Key label under bar
                        val labelPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.argb(180, 148, 163, 184)
                            textSize = 17f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        val shortLabel = if (pair.first.length > 5) pair.first.take(4) + "." else pair.first
                        drawText(shortLabel, x + barWidth / 2, canvasH + 20f, labelPaint)
                    }
                }
            }
        }
    }
}

@Composable
fun HourlyIncidenceChart(
    hourlyData: Map<Int, Int>, // hour 0..23 -> count
    modifier: Modifier = Modifier
) {
    val maxCount = (hourlyData.values.maxOrNull() ?: 1).coerceAtLeast(1)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "INCIDÊNCIA POR FAIXA HORÁRIA (00H - 23H)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "Pico: Madrugada / Noite",
                    fontSize = 11.sp,
                    color = SecAmber,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val canvasW = size.width
                val canvasH = size.height - 20f
                val barW = canvasW / 24f

                for (h in 0..23) {
                    val count = hourlyData[h] ?: 0
                    val barH = (count.toFloat() / maxCount) * canvasH
                    val x = h * barW
                    val y = canvasH - barH

                    val color = when {
                        h in 20..23 || h in 0..4 -> SecRed // High risk night/early morning
                        count >= (maxCount * 0.7) -> SecAmber
                        else -> SecCyanPrimary
                    }

                    drawRect(
                        color = color,
                        topLeft = Offset(x + 2f, y),
                        size = Size(barW - 4f, barH)
                    )

                    // Draw time labels at 0h, 6h, 12h, 18h, 23h
                    if (h % 6 == 0 || h == 23) {
                        drawContext.canvas.nativeCanvas.apply {
                            val textPaint = android.graphics.Paint().apply {
                                this.color = android.graphics.Color.argb(160, 148, 163, 184)
                                textSize = 16f
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                            drawText("${h}h", x + barW / 2, canvasH + 18f, textPaint)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryDonutChart(
    categories: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val total = categories.sumOf { it.second }.coerceAtLeast(1)
    val colors = listOf(
        SecRed, Color(0xFFEA580C), SecAmber, Color(0xFF9333EA),
        SecCyanPrimary, Color(0xFF10B981), Color(0xFF0284C7), Color(0xFF64748B)
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "DISTRIBUIÇÃO CRIMINAL POR CATEGORIA",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Donut
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(130.dp)) {
                        var startAngle = -90f
                        categories.forEachIndexed { i, pair ->
                            val sweepAngle = (pair.second.toFloat() / total) * 360f
                            val color = colors[i % colors.size]
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = 24f, cap = StrokeCap.Butt)
                            )
                            startAngle += sweepAngle
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$total",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "TOTAL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Legend
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.take(5).forEachIndexed { i, pair ->
                        val color = colors[i % colors.size]
                        val pct = (pair.second.toFloat() / total * 100).roundToInt()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${pair.first}: ${pair.second} ($pct%)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
private fun Float.roundToInt(): Int = Math.round(this)
