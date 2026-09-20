package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gis.CartographicData
import com.example.gis.GisEngine
import com.example.gis.IncidentLocationManager
import com.example.ui.theme.SecAmber
import com.example.ui.theme.SecCyanLight
import com.example.ui.theme.SecCyanPrimary
import com.example.ui.theme.SecEmerald
import com.example.ui.theme.SecNavyDark
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun IncidentMapPickerDialog(
    initialLat: Double,
    initialLng: Double,
    initialAccuracy: Float = 3.0f,
    onDismissRequest: () -> Unit,
    onLocationPinned: (lat: Double, lng: Double, accuracy: Float, bairro: String?, municipio: String?, streetAddress: String?) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var pinnedLat by remember { mutableDoubleStateOf(initialLat) }
    var pinnedLng by remember { mutableDoubleStateOf(initialLng) }
    var accuracy by remember { mutableFloatStateOf(initialAccuracy) }
    var isGeocoding by remember { mutableStateOf(false) }
    var isLocatingGps by remember { mutableStateOf(false) }

    var detectedBairro by remember { mutableStateOf<String?>(null) }
    var detectedMunicipio by remember { mutableStateOf<String?>(null) }
    var detectedStreet by remember { mutableStateOf<String?>(null) }

    // Tactical Vector Viewport State (Center and Zoom)
    var centerLat by remember { mutableDoubleStateOf(initialLat) }
    var centerLng by remember { mutableDoubleStateOf(initialLng) }
    var zoomLevel by remember { mutableFloatStateOf(1.4f) } // 0.5 to 8.0

    val luandaZones = remember {
        listOf(
            "Ingombota" to Pair(-8.8140, 13.2320),
            "Maianga" to Pair(-8.8400, 13.2300),
            "Talatona" to Pair(-8.9100, 13.1800),
            "Kilamba" to Pair(-8.9950, 13.2450),
            "Viana" to Pair(-8.9050, 13.3700),
            "Cazenga" to Pair(-8.8050, 13.2950),
            "Cacuaco" to Pair(-8.7750, 13.3650),
            "Belas" to Pair(-8.9800, 13.2400)
        )
    }

    // Trigger initial reverse geocode
    LaunchedEffect(pinnedLat, pinnedLng) {
        isGeocoding = true
        val res = IncidentLocationManager.reverseGeocode(context, pinnedLat, pinnedLng)
        if (res != null) {
            detectedBairro = res.bairro
            detectedMunicipio = res.municipio
            detectedStreet = res.streetAddress
        }
        isGeocoding = false
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0F1D)),
            color = Color(0xFF0A0F1D)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Interactive Tactical Vector GIS Map Canvas
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF070B14))
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                zoomLevel = (zoomLevel * zoom).coerceIn(0.5f, 8.0f)
                                val degreesPerPixel = 0.00032 / zoomLevel
                                centerLng -= pan.x * degreesPerPixel
                                centerLat += pan.y * degreesPerPixel
                            }
                        }
                        .pointerInput(centerLat, centerLng, zoomLevel) {
                            detectTapGestures { tapOffset ->
                                val degreesPerPixel = 0.00032 / zoomLevel
                                val dx = tapOffset.x - (size.width / 2f)
                                val dy = tapOffset.y - (size.height / 2f)
                                pinnedLng = centerLng + (dx * degreesPerPixel)
                                pinnedLat = centerLat - (dy * degreesPerPixel)
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val degreesPerPixel = 0.00032 / zoomLevel
                        val w = size.width
                        val h = size.height

                        // Draw Grid Lines (Tactical Map Coordinate Grid)
                        val gridSpacingPx = 90f
                        var gx = 0f
                        while (gx <= w) {
                            drawLine(
                                color = Color(0x1838BDF8),
                                start = Offset(gx, 0f),
                                end = Offset(gx, h),
                                strokeWidth = 1f
                            )
                            gx += gridSpacingPx
                        }
                        var gy = 0f
                        while (gy <= h) {
                            drawLine(
                                color = Color(0x1838BDF8),
                                start = Offset(0f, gy),
                                end = Offset(w, gy),
                                strokeWidth = 1f
                            )
                            gy += gridSpacingPx
                        }

                        // Draw Luanda Administrative Boundaries & Sectors
                        CartographicData.ADMINISTRATIVE_BOUNDARIES.forEach { boundary ->
                            if (boundary.polygonPoints.size > 2) {
                                val path = Path()
                                boundary.polygonPoints.forEachIndexed { idx, pt ->
                                    val px = (w / 2f) + ((pt.second - centerLng) / degreesPerPixel).toFloat()
                                    val py = (h / 2f) - ((pt.first - centerLat) / degreesPerPixel).toFloat()
                                    if (idx == 0) path.moveTo(px, py) else path.lineTo(px, py)
                                }
                                path.close()
                                drawPath(path, color = Color(0x1406B6D4))
                                drawPath(path, color = Color(0x5538BDF8), style = Stroke(width = 1.4f))
                            }
                        }

                        // Center Viewport Crosshair
                        drawLine(
                            color = Color(0x3338BDF8),
                            start = Offset(w / 2f - 24f, h / 2f),
                            end = Offset(w / 2f + 24f, h / 2f),
                            strokeWidth = 1.5f
                        )
                        drawLine(
                            color = Color(0x3338BDF8),
                            start = Offset(w / 2f, h / 2f - 24f),
                            end = Offset(w / 2f, h / 2f + 24f),
                            strokeWidth = 1.5f
                        )

                        // Draw Pinned Incident Marker
                        val pinPx = (w / 2f) + ((pinnedLng - centerLng) / degreesPerPixel).toFloat()
                        val pinPy = (h / 2f) - ((pinnedLat - centerLat) / degreesPerPixel).toFloat()

                        // Accuracy halo
                        val radiusPx = (accuracy / (degreesPerPixel * 111320.0)).toFloat().coerceIn(16f, 100f)
                        drawCircle(
                            color = Color(0x2E06B6D4),
                            radius = radiusPx,
                            center = Offset(pinPx, pinPy)
                        )
                        drawCircle(
                            color = Color(0xFF06B6D4),
                            radius = radiusPx,
                            center = Offset(pinPx, pinPy),
                            style = Stroke(width = 1.5f)
                        )

                        // Core Pin Circle and Center Dot
                        drawCircle(
                            color = Color(0xFF38BDF8),
                            radius = 9f,
                            center = Offset(pinPx, pinPy)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 4f,
                            center = Offset(pinPx, pinPy)
                        )

                        // Tactical Reticle Lines on Pin
                        drawLine(
                            color = Color(0xFF38BDF8),
                            start = Offset(pinPx - 18f, pinPy),
                            end = Offset(pinPx + 18f, pinPy),
                            strokeWidth = 2f
                        )
                        drawLine(
                            color = Color(0xFF38BDF8),
                            start = Offset(pinPx, pinPy - 18f),
                            end = Offset(pinPx, pinPy + 18f),
                            strokeWidth = 2f
                        )
                    }
                }

                // Top Header Card with Instructions and Close Button
                Surface(
                    color = SecNavyDark.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SecCyanPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = CircleShape,
                                color = SecCyanPrimary.copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = null,
                                        tint = SecCyanPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "FIXAR PONTO NO MAPA GIS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Arraste o mapa e toque no local exato da ocorrência.",
                                    fontSize = 11.sp,
                                    color = SecCyanLight
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Map Floating Controls (Right Side)
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Zoom In
                    FloatingActionButton(
                        onClick = { zoomLevel = (zoomLevel * 1.35f).coerceAtMost(8.0f) },
                        containerColor = SecNavyDark.copy(alpha = 0.9f),
                        contentColor = SecCyanLight,
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Aumentar Zoom", modifier = Modifier.size(20.dp))
                    }

                    // Zoom Out
                    FloatingActionButton(
                        onClick = { zoomLevel = (zoomLevel / 1.35f).coerceAtLeast(0.5f) },
                        containerColor = SecNavyDark.copy(alpha = 0.9f),
                        contentColor = SecCyanLight,
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Diminuir Zoom", modifier = Modifier.size(20.dp))
                    }

                    // Recenter on Pinned Incident
                    FloatingActionButton(
                        onClick = {
                            centerLat = pinnedLat
                            centerLng = pinnedLng
                        },
                        containerColor = SecNavyDark.copy(alpha = 0.9f),
                        contentColor = SecCyanPrimary,
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Place, contentDescription = "Centralizar Marcador", modifier = Modifier.size(20.dp))
                    }

                    // Obtain and Center on Device GPS
                    FloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                isLocatingGps = true
                                val result = IncidentLocationManager.getCurrentPinnedLocation(context)
                                result.onSuccess { loc ->
                                    pinnedLat = loc.latitude
                                    pinnedLng = loc.longitude
                                    centerLat = loc.latitude
                                    centerLng = loc.longitude
                                    accuracy = loc.accuracy
                                    detectedBairro = loc.bairro
                                    detectedMunicipio = loc.municipio
                                    detectedStreet = loc.streetAddress
                                    Toast.makeText(context, "Sinal GPS capturado: ±${loc.accuracy.toInt()}m", Toast.LENGTH_SHORT).show()
                                }.onFailure {
                                    Toast.makeText(context, "Não foi possível obter GPS: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                                isLocatingGps = false
                            }
                        },
                        containerColor = SecCyanPrimary,
                        contentColor = Color.White,
                        modifier = Modifier.size(46.dp),
                        shape = CircleShape
                    ) {
                        if (isLocatingGps) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.MyLocation, contentDescription = "Meu GPS", modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // Bottom Panel with Selected Coordinates, Quick Luanda Zones, Address Detection and Confirmation Button
                Surface(
                    color = SecNavyDark.copy(alpha = 0.96f),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SecCyanPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Quick Zone Selection Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Zona:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            luandaZones.forEach { (name, coords) ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (detectedMunicipio?.contains(name, ignoreCase = true) == true) SecCyanPrimary else Color(0xFF1E293B),
                                    modifier = Modifier.clickable {
                                        pinnedLat = coords.first
                                        pinnedLng = coords.second
                                        centerLat = coords.first
                                        centerLng = coords.second
                                    }
                                ) {
                                    Text(
                                        text = name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (detectedMunicipio?.contains(name, ignoreCase = true) == true) Color.Black else Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Coordinates Display (WGS84 & UTM Zone 33S)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.GpsFixed, contentDescription = null, tint = SecEmerald, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "WGS 84 (Precisão: ±${accuracy.toInt()}m)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SecEmerald
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = String.format(Locale.US, "Lat: %.6f°, Lng: %.6f°", pinnedLat, pinnedLng),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White
                                )
                                Text(
                                    text = "UTM: ${GisEngine.toUtm(pinnedLat, pinnedLng)}",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = SecCyanLight
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SecCyanPrimary.copy(alpha = 0.18f),
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.TouchApp, contentDescription = null, tint = SecCyanPrimary, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ponto Ajustável", fontSize = 10.sp, color = SecCyanLight, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Address / Bairro Detection Preview
                        if (detectedBairro != null || detectedStreet != null || isGeocoding) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isGeocoding) {
                                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = SecCyanPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("A identificar endereço...", fontSize = 11.sp, color = SecCyanLight)
                                    } else {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = SecAmber, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = listOfNotNull(detectedStreet, detectedBairro, detectedMunicipio).joinToString(" • "),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: Cancel and Confirm Pin
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismissRequest,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancelar", color = Color.White, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    onLocationPinned(
                                        pinnedLat,
                                        pinnedLng,
                                        accuracy,
                                        detectedBairro,
                                        detectedMunicipio,
                                        detectedStreet
                                    )
                                    onDismissRequest()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SecCyanPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Confirmar Ponto", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
