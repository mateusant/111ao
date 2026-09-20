package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.model.IncidentEntity
import com.example.gis.CartographicData
import com.example.ui.theme.HeatmapCritical
import com.example.ui.theme.HeatmapExtreme
import com.example.ui.theme.HeatmapHigh
import com.example.ui.theme.HeatmapLow
import com.example.ui.theme.HeatmapMed
import com.example.ui.theme.SecNavyDark
import kotlin.math.hypot

@Composable
fun HeatmapCanvas(
    incidents: List<IncidentEntity>,
    radiusIntensity: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    var centerLat by remember { mutableStateOf(-8.8383) }
    var centerLng by remember { mutableStateOf(13.2344) }
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070C16))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomLevel = (zoomLevel * zoom).coerceIn(0.5f, 5.0f)
                    val degreesPerPixel = 0.0004 / zoomLevel
                    centerLng -= pan.x * degreesPerPixel
                    centerLat += pan.y * degreesPerPixel
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height
            val scale = (canvasW / 0.25f) * zoomLevel

            fun toScreen(lat: Double, lng: Double): Offset {
                val x = canvasW / 2 + (lng - centerLng).toFloat() * scale
                val y = canvasH / 2 - (lat - centerLat).toFloat() * scale
                return Offset(x, y)
            }

            // 1. Draw Subtle Tactical Grid
            val step = 50f * zoomLevel
            var curX = 0f
            while (curX < canvasW) {
                drawLine(Color(0xFF131D31), Offset(curX, 0f), Offset(curX, canvasH), 0.8f)
                curX += step
            }
            var curY = 0f
            while (curY < canvasH) {
                drawLine(Color(0xFF131D31), Offset(0f, curY), Offset(canvasW, curY), 0.8f)
                curY += step
            }

            // 2. Draw Municipal reference boundaries
            CartographicData.ADMINISTRATIVE_BOUNDARIES.forEach { b ->
                val center = toScreen(b.centerLat, b.centerLng)
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(100, 100, 116, 139)
                        textSize = 12f * zoomLevel.coerceIn(0.8f, 1.8f)
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                    drawText(b.name.uppercase(), center.x, center.y, paint)
                }
            }

            // 3. Render Kernel Density Heatmap Blurs
            val baseRadius = 45f * zoomLevel * radiusIntensity

            // First pass: Wide low heat
            incidents.forEach { inc ->
                val s = toScreen(inc.latitude, inc.longitude)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            HeatmapLow.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        center = s,
                        radius = baseRadius * 1.8f
                    ),
                    radius = baseRadius * 1.8f,
                    center = s
                )
            }

            // Second pass: Medium heat
            incidents.forEach { inc ->
                val s = toScreen(inc.latitude, inc.longitude)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            HeatmapMed.copy(alpha = 0.35f),
                            Color.Transparent
                        ),
                        center = s,
                        radius = baseRadius * 1.2f
                    ),
                    radius = baseRadius * 1.2f,
                    center = s
                )
            }

            // Third pass: High/Critical heat centers
            incidents.forEach { inc ->
                val s = toScreen(inc.latitude, inc.longitude)
                val coreColor = if (inc.category == "Homicídio" || inc.priority == "Crítica") {
                    HeatmapExtreme
                } else {
                    HeatmapCritical
                }

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            coreColor.copy(alpha = 0.7f),
                            HeatmapHigh.copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                        center = s,
                        radius = baseRadius * 0.7f
                    ),
                    radius = baseRadius * 0.7f,
                    center = s
                )
            }

            // 4. Hotspot Cluster Identification & Markers
            // Cluster close points
            val clusters = mutableListOf<MutableList<IncidentEntity>>()
            incidents.forEach { inc ->
                var added = false
                for (cluster in clusters) {
                    val rep = cluster.first()
                    val dist = hypot(rep.latitude - inc.latitude, rep.longitude - inc.longitude)
                    if (dist < 0.015) { // roughly 1.5km
                        cluster.add(inc)
                        added = true
                        break
                    }
                }
                if (!added) {
                    clusters.add(mutableListOf(inc))
                }
            }

            // Draw Cluster Rings and Count
            clusters.filter { it.size >= 2 }.forEach { cluster ->
                val avgLat = cluster.map { it.latitude }.average()
                val avgLng = cluster.map { it.longitude }.average()
                val scr = toScreen(avgLat, avgLng)

                drawCircle(
                    color = HeatmapCritical.copy(alpha = 0.3f),
                    radius = 26f,
                    center = scr
                )
                drawCircle(
                    color = HeatmapCritical,
                    radius = 18f,
                    center = scr
                )
                drawCircle(
                    color = Color.White,
                    radius = 18f,
                    center = scr,
                    style = Stroke(width = 2f)
                )

                // Draw count number
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 14f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                    drawText("${cluster.size}", scr.x, scr.y + 5f, paint)
                }
            }
        }
    }
}
