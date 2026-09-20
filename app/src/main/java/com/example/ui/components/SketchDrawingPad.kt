package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SecCyanPrimary
import com.example.ui.theme.SecRed

data class DrawnLine(
    val path: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

@Composable
fun SketchDrawingPad(
    modifier: Modifier = Modifier,
    onSketchSaved: ((lineCount: Int) -> Unit)? = null
) {
    val lines = remember { mutableStateListOf<DrawnLine>() }
    val currentPoints = remember { mutableStateListOf<Offset>() }
    val currentColor = remember { mutableStateOf(Color.White) }
    val currentWidth = remember { mutableStateOf(4f) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CADERNO DE CROQUIS TÉCNICO",
                fontSize = 12.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                color = SecCyanPrimary,
                letterSpacing = 1.sp
            )

            Row {
                IconButton(
                    onClick = {
                        if (lines.isNotEmpty()) lines.removeAt(lines.size - 1)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Desfazer", modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = {
                        lines.clear()
                        currentPoints.clear()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Limpar", tint = SecRed, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Drawing Surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPoints.clear()
                            currentPoints.add(offset)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentPoints.add(change.position)
                        },
                        onDragEnd = {
                            if (currentPoints.isNotEmpty()) {
                                lines.add(
                                    DrawnLine(
                                        path = currentPoints.toList(),
                                        color = currentColor.value,
                                        strokeWidth = currentWidth.value
                                    )
                                )
                                currentPoints.clear()
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background subtle grid
                val step = 20f
                var x = 0f
                while (x < size.width) {
                    drawLine(Color(0xFF1E293B), Offset(x, 0f), Offset(x, size.height), 0.5f)
                    x += step
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(Color(0xFF1E293B), Offset(0f, y), Offset(size.width, y), 0.5f)
                    y += step
                }

                // Render committed lines
                lines.forEach { line ->
                    if (line.path.size > 1) {
                        val p = Path()
                        line.path.forEachIndexed { i, pt ->
                            if (i == 0) p.moveTo(pt.x, pt.y) else p.lineTo(pt.x, pt.y)
                        }
                        drawPath(
                            p,
                            color = line.color,
                            style = Stroke(
                                width = line.strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                // Render current dragging line
                if (currentPoints.size > 1) {
                    val p = Path()
                    currentPoints.forEachIndexed { i, pt ->
                        if (i == 0) p.moveTo(pt.x, pt.y) else p.lineTo(pt.x, pt.y)
                    }
                    drawPath(
                        p,
                        color = currentColor.value,
                        style = Stroke(
                            width = currentWidth.value,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            if (lines.isEmpty() && currentPoints.isEmpty()) {
                Text(
                    text = "Desenhe aqui o croqui do local, vias, veículos ou vestígios",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Color & Brush Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val colors = listOf(Color.White, SecCyanPrimary, SecRed, Color(0xFFF59E0B), Color(0xFF10B981))
                colors.forEach { color ->
                    Surface(
                        color = color,
                        shape = CircleShape,
                        border = if (currentColor.value == color) androidx.compose.foundation.BorderStroke(2.dp, Color.White) else null,
                        modifier = Modifier
                            .size(24.dp)
                            .border(1.dp, Color.Gray, CircleShape)
                            .pointerInput(color) {
                                detectDragGestures(
                                    onDrag = { _, _ -> },
                                    onDragStart = { currentColor.value = color }
                                )
                            }
                    ) {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .clickable { currentColor.value = color })
                    }
                }
            }

            Button(
                onClick = { onSketchSaved?.invoke(lines.size) },
                colors = ButtonDefaults.buttonColors(containerColor = SecCyanPrimary),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Salvar Croqui", fontSize = 12.sp)
            }
        }
    }
}
