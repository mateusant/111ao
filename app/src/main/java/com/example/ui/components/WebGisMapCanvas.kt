package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CartographicSurveyEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.LocationPoiEntity
import com.example.gis.AdministrativeBoundary
import com.example.gis.CartographicData
import com.example.gis.GisEngine
import com.example.ui.theme.SecAmber
import com.example.ui.theme.SecCyanLight
import com.example.ui.theme.SecCyanPrimary
import com.example.ui.theme.SecEmerald
import com.example.ui.theme.SecNavyDark
import com.example.ui.theme.SecRed
import org.json.JSONArray
import java.util.Locale
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt

enum class GisBaseMapType {
    VETORIAL_SIG,
    SATELITE_MILITAR,
    TOPOGRAFICO_TERRENO,
    MODO_NOTURNO
}

enum class GisToolMode {
    INSPECT,
    MEASURE_DISTANCE,
    MEASURE_AREA,
    BUFFER_ANALYSIS
}

@Composable
fun WebGisMapCanvas(
    incidents: List<IncidentEntity>,
    surveys: List<CartographicSurveyEntity> = emptyList(),
    pois: List<LocationPoiEntity> = emptyList(),
    boundaries: List<AdministrativeBoundary> = CartographicData.ADMINISTRATIVE_BOUNDARIES,
    onIncidentSelected: ((IncidentEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Map Viewport state (Center: Luanda -8.8383, 13.2344)
    var centerLat by remember { mutableStateOf(-8.8383) }
    var centerLng by remember { mutableStateOf(13.2344) }
    var zoomLevel by remember { mutableFloatStateOf(1.0f) } // 0.5 to 5.0

    // Coordinate display mode: 0: WGS84, 1: UTM, 2: DMS
    var coordSystemMode by remember { mutableIntStateOf(0) }

    // Layers active toggles
    var showIncidents by remember { mutableStateOf(true) }
    var showPolygons by remember { mutableStateOf(true) }
    var showTraces by remember { mutableStateOf(true) }
    var showPois by remember { mutableStateOf(true) }
    var showBoundaries by remember { mutableStateOf(true) }
    var showGrid by remember { mutableStateOf(true) }
    var baseMapType by remember { mutableStateOf(GisBaseMapType.VETORIAL_SIG) }

    // Tool Mode
    var toolMode by remember { mutableStateOf(GisToolMode.INSPECT) }
    var selectedIncident by remember { mutableStateOf<IncidentEntity?>(null) }
    var selectedPoi by remember { mutableStateOf<LocationPoiEntity?>(null) }
    var selectedSurveyLine by remember { mutableStateOf<CartographicSurveyEntity?>(null) }
    var selectedSurveyPolygon by remember { mutableStateOf<CartographicSurveyEntity?>(null) }
    var selectedBoundary by remember { mutableStateOf<AdministrativeBoundary?>(null) }
    var showLayerMenu by remember { mutableStateOf(false) }

    // Measurement Points
    val measurePoints = remember { mutableStateListOf<Pair<Double, Double>>() }
    var bufferRadiusMeters by remember { mutableFloatStateOf(1000f) }
    var bufferCenter by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    // User live position
    val userGps = remember { Pair(-8.8383, 13.2344) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                when (baseMapType) {
                    GisBaseMapType.SATELITE_MILITAR -> Color(0xFF0F1923)
                    GisBaseMapType.MODO_NOTURNO -> Color(0xFF070B12)
                    GisBaseMapType.TOPOGRAFICO_TERRENO -> Color(0xFFE2E8F0)
                    GisBaseMapType.VETORIAL_SIG -> Color(0xFF0F172A)
                }
            )
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomLevel = (zoomLevel * zoom).coerceIn(0.4f, 6.0f)
                    val degreesPerPixel = 0.0004 / zoomLevel
                    centerLng -= pan.x * degreesPerPixel
                    centerLat += pan.y * degreesPerPixel
                }
            }
            .pointerInput(toolMode, incidents, pois, surveys, boundaries, showIncidents, showPois, showTraces, showPolygons, showBoundaries) {
                detectTapGestures { tapOffset ->
                    // Convert screen tapOffset to Lat/Lng
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val scale = (canvasWidth / 0.25) * zoomLevel

                    val tapLng = centerLng + (tapOffset.x - canvasWidth / 2) / scale
                    val tapLat = centerLat - (tapOffset.y - canvasHeight / 2) / scale

                    when (toolMode) {
                        GisToolMode.INSPECT -> {
                            // 1. Point hit test (Incidents layer)
                            val hitIncident = if (showIncidents) {
                                incidents.minByOrNull {
                                    val dx = (it.longitude - tapLng) * scale
                                    val dy = (tapLat - it.latitude) * scale
                                    hypot(dx, dy)
                                }
                            } else null
                            val distInc = if (hitIncident != null) {
                                hypot(
                                    (hitIncident.longitude - tapLng) * scale,
                                    (tapLat - hitIncident.latitude) * scale
                                ).toFloat()
                            } else Float.MAX_VALUE

                            // 2. Point hit test (POIs layer)
                            val hitPoi = if (showPois) {
                                pois.minByOrNull {
                                    val dx = (it.longitude - tapLng) * scale
                                    val dy = (tapLat - it.latitude) * scale
                                    hypot(dx, dy)
                                }
                            } else null
                            val distPoi = if (hitPoi != null) {
                                hypot(
                                    (hitPoi.longitude - tapLng) * scale,
                                    (tapLat - hitPoi.latitude) * scale
                                ).toFloat()
                            } else Float.MAX_VALUE

                            if (distInc < 65f && distInc <= distPoi) {
                                selectedIncident = hitIncident
                                selectedPoi = null
                                selectedSurveyLine = null
                                selectedSurveyPolygon = null
                                selectedBoundary = null
                                onIncidentSelected?.invoke(hitIncident!!)
                                return@detectTapGestures
                            } else if (distPoi < 65f) {
                                selectedPoi = hitPoi
                                selectedIncident = null
                                selectedSurveyLine = null
                                selectedSurveyPolygon = null
                                selectedBoundary = null
                                return@detectTapGestures
                            }

                            // 3. Line hit test (Survey Lines layer)
                            if (showTraces) {
                                var bestLine: CartographicSurveyEntity? = null
                                var minLineDistPx = Float.MAX_VALUE
                                for (survey in surveys.filter { it.surveyType == "LINHA" }) {
                                    try {
                                        val array = JSONArray(survey.coordinatesJson)
                                        val pts = mutableListOf<Pair<Double, Double>>()
                                        for (i in 0 until array.length()) {
                                            val obj = array.getJSONObject(i)
                                            pts.add(Pair(obj.getDouble("lat"), obj.getDouble("lng")))
                                        }
                                        val meters = GisEngine.distanceToPolylineMeters(tapLat, tapLng, pts)
                                        val distPx = ((meters / 111000.0) * scale).toFloat()
                                        if (distPx < 45f && distPx < minLineDistPx) {
                                            minLineDistPx = distPx
                                            bestLine = survey
                                        }
                                    } catch (_: Exception) {}
                                }
                                if (bestLine != null) {
                                    selectedSurveyLine = bestLine
                                    selectedIncident = null
                                    selectedPoi = null
                                    selectedSurveyPolygon = null
                                    selectedBoundary = null
                                    return@detectTapGestures
                                }
                            }

                            // 4. Polygon hit test (Survey Polygons layer)
                            if (showPolygons) {
                                var hitPoly: CartographicSurveyEntity? = null
                                for (survey in surveys.filter { it.surveyType == "POLIGONO" }) {
                                    try {
                                        val array = JSONArray(survey.coordinatesJson)
                                        val pts = mutableListOf<Pair<Double, Double>>()
                                        for (i in 0 until array.length()) {
                                            val obj = array.getJSONObject(i)
                                            pts.add(Pair(obj.getDouble("lat"), obj.getDouble("lng")))
                                        }
                                        if (GisEngine.isPointInPolygon(tapLat, tapLng, pts)) {
                                            hitPoly = survey
                                            break
                                        }
                                    } catch (_: Exception) {}
                                }
                                if (hitPoly != null) {
                                    selectedSurveyPolygon = hitPoly
                                    selectedIncident = null
                                    selectedPoi = null
                                    selectedSurveyLine = null
                                    selectedBoundary = null
                                    return@detectTapGestures
                                }
                            }

                            // 5. Boundary hit test (Administrative Boundaries layer)
                            if (showBoundaries) {
                                val hitB = boundaries.firstOrNull {
                                    GisEngine.isPointInPolygon(tapLat, tapLng, it.polygonPoints)
                                }
                                if (hitB != null) {
                                    selectedBoundary = hitB
                                    selectedIncident = null
                                    selectedPoi = null
                                    selectedSurveyLine = null
                                    selectedSurveyPolygon = null
                                    return@detectTapGestures
                                }
                            }

                            // Clear selections if tapped outside
                            selectedIncident = null
                            selectedPoi = null
                            selectedSurveyLine = null
                            selectedSurveyPolygon = null
                            selectedBoundary = null
                        }
                        GisToolMode.MEASURE_DISTANCE, GisToolMode.MEASURE_AREA -> {
                            measurePoints.add(Pair(tapLat, tapLng))
                        }
                        GisToolMode.BUFFER_ANALYSIS -> {
                            bufferCenter = Pair(tapLat, tapLng)
                        }
                    }
                }
            }
    ) {
        // Compose Map Canvas Drawing
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height
            val scale = (canvasW / 0.25f) * zoomLevel

            fun toScreen(lat: Double, lng: Double): Offset {
                val x = canvasW / 2 + (lng - centerLng).toFloat() * scale
                val y = canvasH / 2 - (lat - centerLat).toFloat() * scale
                return Offset(x, y)
            }

            // 1. Draw Grid lines (Cartographic UTM/WGS84 Grid)
            if (showGrid) {
                val gridColor = when (baseMapType) {
                    GisBaseMapType.TOPOGRAFICO_TERRENO -> Color(0xFFCBD5E1)
                    else -> Color(0xFF1E293B)
                }
                val step = 60f * zoomLevel
                var curX = 0f
                while (curX < canvasW) {
                    drawLine(
                        color = gridColor,
                        start = Offset(curX, 0f),
                        end = Offset(curX, canvasH),
                        strokeWidth = 1f
                    )
                    curX += step
                }
                var curY = 0f
                while (curY < canvasH) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, curY),
                        end = Offset(canvasW, curY),
                        strokeWidth = 1f
                    )
                    curY += step
                }
            }

            // 2. Draw Roads / Tactical Corridors
            val roadColor = when (baseMapType) {
                GisBaseMapType.TOPOGRAFICO_TERRENO -> Color(0xFF94A3B8)
                else -> Color(0xFF334155)
            }
            val roads = listOf(
                listOf(Pair(-8.8100, 13.2200), Pair(-8.8350, 13.2500), Pair(-8.8700, 13.2900), Pair(-8.9100, 13.3400)), // Deolinda Rodrigues
                listOf(Pair(-8.8100, 13.2400), Pair(-8.8500, 13.2300), Pair(-8.9200, 13.2350), Pair(-9.0000, 13.2450)), // Estrada da Samba
                listOf(Pair(-8.8300, 13.2100), Pair(-8.8400, 13.2700), Pair(-8.8600, 13.3300)) // Eixo Leste-Oeste
            )
            roads.forEach { road ->
                val roadPath = Path()
                road.forEachIndexed { i, pt ->
                    val scr = toScreen(pt.first, pt.second)
                    if (i == 0) roadPath.moveTo(scr.x, scr.y) else roadPath.lineTo(scr.x, scr.y)
                }
                drawPath(roadPath, roadColor, style = Stroke(width = 3.5f * zoomLevel.coerceIn(0.7f, 2.5f)))
            }

            // 3. Draw Administrative Boundaries (Bairros / Municípios)
            if (showBoundaries) {
                boundaries.forEach { boundary ->
                    if (boundary.polygonPoints.size > 2) {
                        val polyPath = Path()
                        boundary.polygonPoints.forEachIndexed { idx, pt ->
                            val scr = toScreen(pt.first, pt.second)
                            if (idx == 0) polyPath.moveTo(scr.x, scr.y) else polyPath.lineTo(scr.x, scr.y)
                        }
                        polyPath.close()

                        val isSelectedBoundary = boundary.name == selectedBoundary?.name

                        // Fill semi-transparent
                        drawPath(
                            polyPath,
                            if (isSelectedBoundary) SecCyanPrimary.copy(alpha = 0.22f) else SecCyanPrimary.copy(alpha = 0.08f)
                        )
                        // Stroke
                        drawPath(
                            polyPath,
                            if (isSelectedBoundary) Color.White else SecCyanPrimary.copy(alpha = 0.45f),
                            style = Stroke(
                                width = if (isSelectedBoundary) 3f else 1.5f,
                                pathEffect = if (isSelectedBoundary) null else PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)
                            )
                        )

                        // Boundary center label
                        val centerScr = toScreen(boundary.centerLat, boundary.centerLng)
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = if (isSelectedBoundary) android.graphics.Color.WHITE else android.graphics.Color.argb(180, 148, 163, 184)
                                textSize = (11f * zoomLevel).coerceIn(10f, 16f)
                                isFakeBoldText = true
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                            drawText(boundary.name.uppercase(), centerScr.x, centerScr.y, paint)
                        }
                    }
                }
            }

            // 4. Draw Cartographic Surveys (Geoshape & Geotrace)
            surveys.forEach { survey ->
                try {
                    val array = JSONArray(survey.coordinatesJson)
                    val points = mutableListOf<Pair<Double, Double>>()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        points.add(Pair(obj.getDouble("lat"), obj.getDouble("lng")))
                    }

                    val isSelectedPoly = survey.surveyCode == selectedSurveyPolygon?.surveyCode
                    val isSelectedLine = survey.surveyCode == selectedSurveyLine?.surveyCode

                    if (survey.surveyType == "POLIGONO" && points.size >= 3 && showPolygons) {
                        val pPath = Path()
                        points.forEachIndexed { i, p ->
                            val s = toScreen(p.first, p.second)
                            if (i == 0) pPath.moveTo(s.x, s.y) else pPath.lineTo(s.x, s.y)
                        }
                        pPath.close()

                        drawPath(
                            pPath,
                            if (isSelectedPoly) SecCyanPrimary.copy(alpha = 0.35f) else SecRed.copy(alpha = 0.15f)
                        )
                        drawPath(
                            pPath,
                            if (isSelectedPoly) Color.White else SecRed.copy(alpha = 0.8f),
                            style = Stroke(width = if (isSelectedPoly) 4f else 2.5f)
                        )

                        // Draw vertex dots
                        points.forEach { p ->
                            val s = toScreen(p.first, p.second)
                            drawCircle(color = if (isSelectedPoly) Color.White else SecRed, radius = if (isSelectedPoly) 5f else 3f, center = s)
                        }
                    } else if (survey.surveyType == "LINHA" && points.size >= 2 && showTraces) {
                        val lPath = Path()
                        points.forEachIndexed { i, p ->
                            val s = toScreen(p.first, p.second)
                            if (i == 0) lPath.moveTo(s.x, s.y) else lPath.lineTo(s.x, s.y)
                        }

                        if (isSelectedLine) {
                            // Outer glow
                            drawPath(lPath, SecAmber.copy(alpha = 0.4f), style = Stroke(width = 8f))
                            drawPath(lPath, Color.White, style = Stroke(width = 3.5f))
                        } else {
                            drawPath(lPath, SecAmber, style = Stroke(width = 3.5f))
                        }

                        // Draw vertex dots
                        points.forEach { p ->
                            val s = toScreen(p.first, p.second)
                            drawCircle(color = if (isSelectedLine) Color.White else SecAmber, radius = if (isSelectedLine) 5f else 3f, center = s)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore JSON parsing issues
                }
            }

            // 5. Draw Buffer Circle if active
            bufferCenter?.let { bc ->
                val centerScr = toScreen(bc.first, bc.second)
                // Convert buffer radius meters to screen radius
                // 1 deg lat ~ 111,000m
                val radiusDeg = bufferRadiusMeters / 111000.0
                val radiusPx = (radiusDeg * scale).toFloat()

                drawCircle(
                    color = SecCyanPrimary.copy(alpha = 0.2f),
                    radius = radiusPx,
                    center = centerScr
                )
                drawCircle(
                    color = SecCyanPrimary,
                    radius = radiusPx,
                    center = centerScr,
                    style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f))
                )
                drawCircle(color = SecCyanPrimary, radius = 5f, center = centerScr)
            }

            // 6. Draw Measurement Points & Lines
            if (measurePoints.isNotEmpty()) {
                val mPath = Path()
                measurePoints.forEachIndexed { i, p ->
                    val s = toScreen(p.first, p.second)
                    if (i == 0) mPath.moveTo(s.x, s.y) else mPath.lineTo(s.x, s.y)
                    drawCircle(color = SecAmber, radius = 5f, center = s)
                }
                if (toolMode == GisToolMode.MEASURE_AREA && measurePoints.size >= 3) {
                    mPath.close()
                    drawPath(mPath, SecAmber.copy(alpha = 0.25f))
                }
                drawPath(mPath, SecAmber, style = Stroke(width = 2.5f))
            }

            // 7. Draw POIs (Points of Interest)
            if (showPois) {
                pois.forEach { poi ->
                    val s = toScreen(poi.latitude, poi.longitude)
                    if (s.x >= -80f && s.x <= canvasW + 80f && s.y >= -80f && s.y <= canvasH + 80f) {
                        val isPoiSelected = poi.code == selectedPoi?.code
                        val poiColor = when (poi.type) {
                            "Esquadra Policial" -> SecCyanPrimary
                            "Hospital" -> Color(0xFF10B981)
                            "Banco" -> SecAmber
                            "Ponto Crítico" -> Color(0xFFEF4444)
                            else -> Color(0xFF38BDF8)
                        }
                        if (isPoiSelected) {
                            drawCircle(color = poiColor.copy(alpha = 0.40f), radius = 26f, center = s)
                            drawCircle(color = Color.White, radius = 16f, center = s)
                            drawCircle(color = poiColor, radius = 12f, center = s)
                            drawCircle(color = Color.White, radius = 5f, center = s)
                        } else {
                            // Drop shadow
                            drawCircle(color = Color.Black.copy(alpha = 0.35f), radius = 14f, center = Offset(s.x, s.y + 2f))
                            drawCircle(color = Color.White, radius = 13f, center = s)
                            drawCircle(color = poiColor, radius = 10f, center = s)
                            drawCircle(color = Color.White, radius = 4f, center = s)
                        }
                    }
                }
            }

            // 8. Draw Incidents (Ocorrências)
            if (showIncidents) {
                incidents.forEach { inc ->
                    val s = toScreen(inc.latitude, inc.longitude)
                    if (s.x >= -80f && s.x <= canvasW + 80f && s.y >= -80f && s.y <= canvasH + 80f) {
                        val catColor = when (inc.category) {
                            "Homicídio" -> SecRed
                            "Roubo" -> Color(0xFFEA580C)
                            "Furto" -> SecAmber
                            "Tráfico" -> Color(0xFF9333EA)
                            "Acidente" -> Color(0xFF0284C7)
                            "Violência", "Agressão" -> Color(0xFFE11D48)
                            else -> Color(0xFF64748B)
                        }

                        val isSelected = inc.incidentNumber == selectedIncident?.incidentNumber
                        if (isSelected) {
                            // Outer pulsating beacon ring
                            drawCircle(color = catColor.copy(alpha = 0.45f), radius = 28f, center = s)
                            drawCircle(color = Color.White, radius = 18f, center = s)
                            drawCircle(color = catColor, radius = 14f, center = s)
                            drawCircle(color = Color.White, radius = 6f, center = s)
                        } else {
                            // High contrast pin with drop shadow
                            drawCircle(color = Color.Black.copy(alpha = 0.45f), radius = 16f, center = Offset(s.x, s.y + 2f))
                            drawCircle(color = Color.White, radius = 14f, center = s)
                            drawCircle(color = catColor, radius = 10.5f, center = s)
                            drawCircle(color = Color.White, radius = 3.5f, center = s)
                        }
                    }
                }
            }

            // 9. Draw Current Authorized User Position
            val userScr = toScreen(userGps.first, userGps.second)
            drawCircle(color = SecCyanLight.copy(alpha = 0.25f), radius = 22f, center = userScr)
            drawCircle(color = Color.White, radius = 8f, center = userScr)
            drawCircle(color = SecCyanPrimary, radius = 6f, center = userScr)
        }

        // ==========================================
        // OVERLAYS & ON-SCREEN CONTROLS
        // ==========================================

        // Top Status Bar: Coordinates readout + System switcher
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
                .clickable {
                    coordSystemMode = (coordSystemMode + 1) % 3
                }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = null,
                    tint = SecCyanPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val coordText = when (coordSystemMode) {
                    0 -> String.format(Locale.US, "WGS84: %.5f°, %.5f°", centerLat, centerLng)
                    1 -> "UTM: ${GisEngine.toUtm(centerLat, centerLng)}"
                    else -> GisEngine.toDmsFormatted(centerLat, centerLng)
                }
                Text(
                    text = coordText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = SecCyanPrimary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = when (coordSystemMode) {
                            0 -> "WGS 84"
                            1 -> "UTM"
                            else -> "DMS"
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = SecCyanPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Right Floating Action Toolbar: Zoom, Center User, Tools, Layers
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Fit All Data (Enquadrar Pontos, Linhas e Polígonos)
            FloatingGisButton(
                icon = Icons.Default.CenterFocusStrong,
                contentDescription = "Enquadrar Dados",
                onClick = {
                    val surveyPts = surveys.flatMap { survey ->
                        try {
                            val arr = JSONArray(survey.coordinatesJson)
                            (0 until arr.length()).map { Pair(arr.getJSONObject(it).getDouble("lat"), arr.getJSONObject(it).getDouble("lng")) }
                        } catch (_: Exception) { emptyList<Pair<Double, Double>>() }
                    }
                    val allPoints = incidents.map { Pair(it.latitude, it.longitude) } +
                        pois.map { Pair(it.latitude, it.longitude) } +
                        surveyPts
                    if (allPoints.isNotEmpty()) {
                        val minLat = allPoints.minOf { it.first }
                        val maxLat = allPoints.maxOf { it.first }
                        val minLng = allPoints.minOf { it.second }
                        val maxLng = allPoints.maxOf { it.second }
                        centerLat = (minLat + maxLat) / 2.0
                        centerLng = (minLng + maxLng) / 2.0
                        val span = maxOf(maxLat - minLat, maxLng - minLng).coerceAtLeast(0.02)
                        zoomLevel = (0.24 / span).toFloat().coerceIn(0.7f, 3.5f)
                    } else {
                        centerLat = -8.8383
                        centerLng = 13.2344
                        zoomLevel = 1.0f
                    }
                }
            )

            // My Location Button
            FloatingGisButton(
                icon = Icons.Default.MyLocation,
                contentDescription = "Minha Posição",
                onClick = {
                    centerLat = userGps.first
                    centerLng = userGps.second
                    zoomLevel = 1.4f
                }
            )

            // Zoom In
            FloatingGisButton(
                icon = Icons.Default.Add,
                contentDescription = "Aumentar Zoom",
                onClick = { zoomLevel = (zoomLevel * 1.3f).coerceAtMost(6.0f) }
            )

            // Zoom Out
            FloatingGisButton(
                icon = Icons.Default.Remove,
                contentDescription = "Diminuir Zoom",
                onClick = { zoomLevel = (zoomLevel / 1.3f).coerceAtLeast(0.4f) }
            )

            // Layer Switcher Toggle
            FloatingGisButton(
                icon = Icons.Default.Layers,
                contentDescription = "Camadas SIG",
                isActive = showLayerMenu,
                onClick = { showLayerMenu = !showLayerMenu }
            )

            // Measure Tool Toggle (Distance / Area / Buffer)
            FloatingGisButton(
                icon = when (toolMode) {
                    GisToolMode.MEASURE_DISTANCE -> Icons.Default.Straighten
                    GisToolMode.MEASURE_AREA -> Icons.Default.SquareFoot
                    GisToolMode.BUFFER_ANALYSIS -> Icons.Default.NearMe
                    else -> Icons.Default.Straighten
                },
                contentDescription = "Ferramentas de Medição",
                isActive = toolMode != GisToolMode.INSPECT,
                onClick = {
                    toolMode = when (toolMode) {
                        GisToolMode.INSPECT -> GisToolMode.MEASURE_DISTANCE
                        GisToolMode.MEASURE_DISTANCE -> GisToolMode.MEASURE_AREA
                        GisToolMode.MEASURE_AREA -> GisToolMode.BUFFER_ANALYSIS
                        GisToolMode.BUFFER_ANALYSIS -> {
                            measurePoints.clear()
                            bufferCenter = null
                            GisToolMode.INSPECT
                        }
                    }
                }
            )
        }

        // Layers Menu Pop-up
        AnimatedVisibility(
            visible = showLayerMenu,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 50.dp, end = 12.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                modifier = Modifier.width(240.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "CAMADAS SIG (VETORIAIS)",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = SecCyanPrimary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LayerToggleRow("📍 Ocorrências (${incidents.size})", showIncidents) { showIncidents = it }
                    LayerToggleRow("📍 Postos POI (${pois.size})", showPois) { showPois = it }
                    val linesCount = surveys.count { it.surveyType == "LINHA" }
                    LayerToggleRow("〰️ Rotas/Linhas ($linesCount)", showTraces) { showTraces = it }
                    val polyCount = surveys.count { it.surveyType == "POLIGONO" }
                    LayerToggleRow("⬡ Polígonos/Zonas ($polyCount)", showPolygons) { showPolygons = it }
                    LayerToggleRow("🏛️ Limites Luanda (${boundaries.size})", showBoundaries) { showBoundaries = it }
                    LayerToggleRow("🌐 Malha Táctica WGS84", showGrid) { showGrid = it }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "MAPA BASE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BaseMapChip("Vetorial", baseMapType == GisBaseMapType.VETORIAL_SIG) {
                            baseMapType = GisBaseMapType.VETORIAL_SIG
                        }
                        BaseMapChip("Satélite", baseMapType == GisBaseMapType.SATELITE_MILITAR) {
                            baseMapType = GisBaseMapType.SATELITE_MILITAR
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BaseMapChip("Terreno", baseMapType == GisBaseMapType.TOPOGRAFICO_TERRENO) {
                            baseMapType = GisBaseMapType.TOPOGRAFICO_TERRENO
                        }
                        BaseMapChip("Noturno", baseMapType == GisBaseMapType.MODO_NOTURNO) {
                            baseMapType = GisBaseMapType.MODO_NOTURNO
                        }
                    }
                }
            }
        }

        // Active Tool Banner (Distance / Area / Buffer)
        AnimatedVisibility(
            visible = toolMode != GisToolMode.INSPECT,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 50.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SecAmber.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (toolMode) {
                                GisToolMode.MEASURE_DISTANCE -> Icons.Default.Straighten
                                GisToolMode.MEASURE_AREA -> Icons.Default.SquareFoot
                                else -> Icons.Default.NearMe
                            },
                            contentDescription = null,
                            tint = SecAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (toolMode) {
                                GisToolMode.MEASURE_DISTANCE -> "Medição de Distância"
                                GisToolMode.MEASURE_AREA -> "Medição de Área"
                                else -> "Análise de Buffer"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = SecAmber
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable {
                                    toolMode = GisToolMode.INSPECT
                                    measurePoints.clear()
                                    bufferCenter = null
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (toolMode == GisToolMode.MEASURE_DISTANCE) {
                        var totalMeters = 0.0
                        for (i in 0 until measurePoints.size - 1) {
                            totalMeters += GisEngine.haversineDistanceMeters(
                                measurePoints[i].first, measurePoints[i].second,
                                measurePoints[i + 1].first, measurePoints[i + 1].second
                            )
                        }
                        Text(
                            text = if (totalMeters >= 1000) {
                                String.format(Locale.US, "Comprimento: %.2f km (%d pontos)", totalMeters / 1000.0, measurePoints.size)
                            } else {
                                String.format(Locale.US, "Comprimento: %.1f m (%d pontos)", totalMeters, measurePoints.size)
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text("Toque no mapa para adicionar vértices", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else if (toolMode == GisToolMode.MEASURE_AREA) {
                        val areaM2 = GisEngine.calculatePolygonAreaM2(measurePoints.toList())
                        val ha = areaM2 / 10000.0
                        Text(
                            text = String.format(Locale.US, "Área: %.1f m² (%.2f ha)", areaM2, ha),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text("Mínimo 3 pontos para fechar o polígono", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else if (toolMode == GisToolMode.BUFFER_ANALYSIS) {
                        bufferCenter?.let { bc ->
                            // Count incidents inside buffer
                            val count = incidents.count { inc ->
                                GisEngine.haversineDistanceMeters(bc.first, bc.second, inc.latitude, inc.longitude) <= bufferRadiusMeters
                            }
                            Text(
                                text = "Raio: ${bufferRadiusMeters.toInt()}m | Ocorrências: $count",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } ?: Text("Toque no mapa para posicionar o centro do buffer", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Bottom Left: Graphic Scale Bar (Escala Gráfica)
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                // Approximate scale bar calculation
                val scaleMeters = (1000.0 / zoomLevel).roundToInt()
                val label = if (scaleMeters >= 1000) "${scaleMeters / 1000} km" else "$scaleMeters m"

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(4.dp)
                            .background(SecCyanPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Bottom Selected Incident Inspector Card
        selectedIncident?.let { inc ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = inc.incidentNumber,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            CategoryBadge(inc.category)
                        }
                        IconButton(
                            onClick = { selectedIncident = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = inc.incidentType,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "📍 ${inc.locationName} (${inc.bairro}, ${inc.municipio})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🗓️ ${inc.date} às ${inc.time} • 👮 ${inc.agentName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        SyncStatusBadge(inc.syncStatus, showLabel = false)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { onIncidentSelected?.invoke(inc) },
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Ver Detalhes Completos", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Bottom Selected POI Inspector Card
        selectedPoi?.let { poi ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SecCyanPrimary.copy(alpha = 0.15f),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = poi.code,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = SecCyanPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = poi.type,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = { selectedPoi = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = poi.name,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "📍 ${poi.bairro}, ${poi.municipio}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🌐 Lat: ${String.format(Locale.US, "%.5f", poi.latitude)} | Lng: ${String.format(Locale.US, "%.5f", poi.longitude)}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (poi.riskLevel) {
                                "Crítico" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                "Alerta" -> SecAmber.copy(alpha = 0.15f)
                                else -> Color(0xFF10B981).copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = poi.riskLevel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (poi.riskLevel) {
                                    "Crítico" -> Color(0xFFEF4444)
                                    "Alerta" -> SecAmber
                                    else -> Color(0xFF10B981)
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Selected Survey Line Inspector Card
        selectedSurveyLine?.let { line ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SecAmber.copy(alpha = 0.8f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SecAmber.copy(alpha = 0.15f),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = line.surveyCode,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = SecAmber,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "ROTA / TRANSECTO (LINHA)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = SecAmber
                            )
                        }
                        IconButton(
                            onClick = { selectedSurveyLine = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = line.title,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    val lengthStr = if (line.lengthMeters >= 1000.0) {
                        String.format(Locale.US, "%.2f km (%.0f metros)", line.lengthMeters / 1000.0, line.lengthMeters)
                    } else {
                        String.format(Locale.US, "%.1f metros", line.lengthMeters)
                    }

                    Text(
                        text = "📏 Extensão da Linha: $lengthStr",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "📍 Local: ${line.bairro}, ${line.municipio} • Agente: ${line.agentCode}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (line.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📝 ${line.notes}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Bottom Selected Survey Polygon Inspector Card
        selectedSurveyPolygon?.let { poly ->
            val polyPts = remember(poly) {
                try {
                    val arr = JSONArray(poly.coordinatesJson)
                    (0 until arr.length()).map { Pair(arr.getJSONObject(it).getDouble("lat"), arr.getJSONObject(it).getDouble("lng")) }
                } catch (_: Exception) { emptyList<Pair<Double, Double>>() }
            }
            val enclosedIncidentsCount = remember(poly, incidents) {
                if (polyPts.size >= 3) {
                    incidents.count { inc -> GisEngine.isPointInPolygon(inc.latitude, inc.longitude, polyPts) }
                } else 0
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SecCyanPrimary.copy(alpha = 0.8f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SecCyanPrimary.copy(alpha = 0.15f),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = poly.surveyCode,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = SecCyanPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "ZONA CRÍTICA (POLÍGONO)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = SecCyanPrimary
                            )
                        }
                        IconButton(
                            onClick = { selectedSurveyPolygon = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = poly.title,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val areaHa = poly.areaM2 / 10000.0
                    Text(
                        text = String.format(Locale.US, "📐 Área Mapeada: %.1f m² (%.2f hectares)", poly.areaM2, areaHa),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📍 ${poly.bairro}, ${poly.municipio} • Agente: ${poly.agentCode}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (enclosedIncidentsCount > 0) Color(0xFFEF4444).copy(alpha = 0.15f) else SecEmerald.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "🎯 $enclosedIncidentsCount Ocorrências no Polígono",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (enclosedIncidentsCount > 0) Color(0xFFEF4444) else SecEmerald,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Selected Administrative Boundary Inspector Card
        selectedBoundary?.let { b ->
            val boundaryIncidentsCount = remember(b, incidents) {
                incidents.count { inc -> GisEngine.isPointInPolygon(inc.latitude, inc.longitude, b.polygonPoints) }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = b.type,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                color = SecCyanPrimary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = b.name.uppercase(),
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = { selectedBoundary = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "🌐 Centro: ${GisEngine.toUtm(b.centerLat, b.centerLng)}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SecCyanPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "📊 $boundaryIncidentsCount Ocorrências Registadas neste Território",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecCyanPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingGisButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = if (isActive) SecCyanPrimary else MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) SecCyanLight else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        shadowElevation = 4.dp,
        modifier = Modifier
            .size(42.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun LayerToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(width = 38.dp, height = 24.dp)
        )
    }
}

@Composable
private fun BaseMapChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) SecCyanPrimary else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(92.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
