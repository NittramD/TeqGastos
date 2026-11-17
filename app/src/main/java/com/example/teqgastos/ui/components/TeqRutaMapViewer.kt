package com.example.teqgastos.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.teqgastos.model.CoordenadaGps
import com.example.teqgastos.model.TrasladoTeqRuta
import com.example.teqgastos.ui.theme.TeqPrimary
import com.example.teqgastos.ui.theme.TeqSecondary
import com.example.teqgastos.ui.theme.TeqSuccess
import com.example.teqgastos.ui.theme.TeqWarning

/**
 * Visor de Mapas Geográficos de Tramos Recorridos en TeqRuta
 * Soporta renderizado dinámico interactivo con Leaflet/OpenStreetMap y
 * un motor de proyección vectorial en Canvas para funcionamiento 100% offline.
 */
@Composable
fun TeqRutaMapViewer(
    traslados: List<TrasladoTeqRuta>,
    modifier: Modifier = Modifier,
    selectedTramoId: String? = null,
    isOfflineForced: Boolean = false,
    showControlsOverlay: Boolean = true
) {
    val context = LocalContext.current
    var isCanvasMode by remember { mutableStateOf(isOfflineForced) }
    var activeTrasladoIndex by remember(traslados, selectedTramoId) {
        val found = traslados.indexOfFirst { it.id == selectedTramoId }
        mutableStateOf(if (found >= 0) found else 0)
    }

    val currentTraslado = traslados.getOrNull(activeTrasladoIndex) ?: traslados.firstOrNull()

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF0F172A))
    ) {
        if (currentTraslado != null) {
            if (!isCanvasMode) {
                // Modo Interactivo Online (Leaflet + OpenStreetMap)
                LeafletWebViewMap(
                    traslado = currentTraslado,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Modo Offline Autónomo (Vector Canvas con Proyección Geográfica)
                TeqRutaOfflineVectorMap(
                    traslado = currentTraslado,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Barra Superior de Controles y Selección de Tramos
            if (showControlsOverlay) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .align(Alignment.TopCenter)
                ) {
                    // Tarjeta de Metadatos del Tramo en Trámite
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AltRoute,
                                        contentDescription = null,
                                        tint = TeqPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = currentTraslado.tramo,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                                Surface(
                                    color = if (isCanvasMode) TeqWarning.copy(alpha = 0.15f) else TeqSuccess.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (isCanvasMode) "Offline" else "OSM Satelital",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isCanvasMode) TeqWarning else TeqSuccess,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Métricas de Viaje
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Distancia", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text("${currentTraslado.kilometros} km", fontWeight = FontWeight.Bold, color = TeqPrimary)
                                }
                                Column {
                                    Text("Tiempo Est.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text(currentTraslado.tiempoEstimado, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Column {
                                    Text("Peajes TAG", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text(currentTraslado.peajesClp.formatClp(), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Column {
                                    Text("Combustible", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text(currentTraslado.combustibleClp.formatClp(), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }

                // Botonera Flotante Inferior de Acción y Navegación GPS Externa
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { isCanvasMode = !isCanvasMode },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isCanvasMode) Icons.Default.Explore else Icons.Default.DirectionsCar,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isCanvasMode) "Ver Mapa Web" else "Ver Vector Offline",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                launchExternalGpsNavigation(
                                    context = context,
                                    origenLat = currentTraslado.origenLat,
                                    origenLng = currentTraslado.origenLng,
                                    destinoLat = currentTraslado.destinoLat,
                                    destinoLng = currentTraslado.destinoLng,
                                    destinoNombre = currentTraslado.destinoNombre
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = TeqPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Abrir GPS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renderizado de Mapa Interactivo mediante WebView con Leaflet y OpenStreetMap / CartoDB Voyager.
 * 100% libre de credenciales o billing de terceros.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun LeafletWebViewMap(
    traslado: TrasladoTeqRuta,
    modifier: Modifier = Modifier
) {
    val allPoints = remember(traslado) {
        val list = mutableListOf<Pair<Double, Double>>()
        list.add(Pair(traslado.origenLat, traslado.origenLng))
        traslado.puntosRuta.forEach { list.add(Pair(it.lat, it.lng)) }
        list.add(Pair(traslado.destinoLat, traslado.destinoLng))
        list
    }

    val polylineJson = remember(allPoints) {
        allPoints.joinToString(prefix = "[", postfix = "]") { "[${it.first}, ${it.second}]" }
    }

    val htmlContent = remember(traslado, polylineJson) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body, #map {
                    width: 100%;
                    height: 100%;
                    margin: 0;
                    padding: 0;
                    background-color: #0F172A;
                }
                .leaflet-popup-content-wrapper {
                    border-radius: 12px;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                    box-shadow: 0 8px 24px rgba(0,0,0,0.25);
                }
                .badge-hospital {
                    background: #0056B3;
                    color: white;
                    padding: 2px 8px;
                    border-radius: 6px;
                    font-weight: bold;
                    font-size: 11px;
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', { zoomControl: false }).setView([${traslado.origenLat}, ${traslado.origenLng}], 10);
                
                L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
                    maxZoom: 19,
                    attribution: '&copy; OpenStreetMap &copy; CartoDB'
                }).addTo(map);

                var routePoints = $polylineJson;

                // Trazado de Ruta con Brillo y Color TEQMED
                var routeGlow = L.polyline(routePoints, {
                    color: '#00E5FF',
                    weight: 8,
                    opacity: 0.4
                }).addTo(map);

                var routeLine = L.polyline(routePoints, {
                    color: '#0056B3',
                    weight: 5,
                    opacity: 0.9,
                    lineJoin: 'round'
                }).addTo(map);

                // Marcador de Origen
                var startIcon = L.divIcon({
                    html: '<div style="background-color:#2E7D32;width:18px;height:18px;border-radius:50%;border:3px solid #FFFFFF;box-shadow:0 0 10px rgba(0,0,0,0.5);"></div>',
                    className: 'start-pin',
                    iconSize: [18, 18],
                    iconAnchor: [9, 9]
                });
                L.marker([${traslado.origenLat}, ${traslado.origenLng}], { icon: startIcon })
                    .addTo(map)
                    .bindPopup('<b>Origen:</b> ${traslado.origenNombre}<br><span class="badge-hospital">Base TEQMED</span>');

                // Marcador de Destino Hospitalario
                var endIcon = L.divIcon({
                    html: '<div style="background-color:#D32F2F;width:22px;height:22px;border-radius:50%;border:3px solid #FFFFFF;box-shadow:0 0 12px rgba(211,47,47,0.8);"><div style="color:white;font-size:12px;text-align:center;line-height:16px;font-weight:bold;">+</div></div>',
                    className: 'end-pin',
                    iconSize: [22, 22],
                    iconAnchor: [11, 11]
                });
                L.marker([${traslado.destinoLat}, ${traslado.destinoLng}], { icon: endIcon })
                    .addTo(map)
                    .bindPopup('<b>Destino:</b> ${traslado.destinoNombre}<br>Distancia: ${traslado.kilometros} km')
                    .openPopup();

                // Ajustar vista a toda la extensión del tramo recorrido
                map.fitBounds(routeLine.getBounds(), { padding: [50, 50] });
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    useWideViewPort = true
                    loadWithOverviewMode = true
                }
                webViewClient = WebViewClient()
                loadDataWithBaseURL("https://cartocdn.com", htmlContent, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://cartocdn.com", htmlContent, "text/html", "UTF-8", null)
        },
        modifier = modifier
    )
}

/**
 * Motor de Dibujo Vectorial Nativo en Canvas para visualización 100% offline del tramo recorrido.
 * Proyecta matemáticamente las coordenadas geográficas (lat, lng) en el lienzo con cuadrícula,
 * polilínea en gradiente, marcadores de hospitales y vehículo técnico en ruta.
 */
@Composable
private fun TeqRutaOfflineVectorMap(
    traslado: TrasladoTeqRuta,
    modifier: Modifier = Modifier
) {
    val allCoords = remember(traslado) {
        val list = mutableListOf(CoordenadaGps(traslado.origenLat, traslado.origenLng, traslado.origenNombre))
        list.addAll(traslado.puntosRuta)
        list.add(CoordenadaGps(traslado.destinoLat, traslado.destinoLng, traslado.destinoNombre))
        list
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 90.dp)) {
            val width = size.width
            val height = size.height

            // Encontrar límites de latitud y longitud
            val minLat = allCoords.minOf { it.lat }
            val maxLat = allCoords.maxOf { it.lat }
            val minLng = allCoords.minOf { it.lng }
            val maxLng = allCoords.maxOf { it.lng }

            val latSpan = (maxLat - minLat).coerceAtLeast(0.05)
            val lngSpan = (maxLng - minLng).coerceAtLeast(0.05)

            // Función de proyección geográfica a coordenadas de Canvas
            fun project(lat: Double, lng: Double): Offset {
                val xNorm = ((lng - minLng) / lngSpan).toFloat()
                val yNorm = 1f - ((lat - minLat) / latSpan).toFloat() // Invertir Y porque en pantalla crece hacia abajo
                return Offset(
                    x = (xNorm * (width - 100f)) + 50f,
                    y = (yNorm * (height - 120f)) + 60f
                )
            }

            // 1. Cuadrícula de Coordenadas de Terreno (Grid de GPS)
            val gridPaint = Color(0x18FFFFFF)
            for (i in 0..4) {
                val x = width * (i / 4f)
                drawLine(gridPaint, Offset(x, 0f), Offset(x, height), strokeWidth = 1f)
                val y = height * (i / 4f)
                drawLine(gridPaint, Offset(0f, y), Offset(width, y), strokeWidth = 1f)
            }

            // 2. Trazo de la Ruta (Polyline con resplandor neón)
            val path = Path()
            val offsets = allCoords.map { project(it.lat, it.lng) }

            offsets.forEachIndexed { index, offset ->
                if (index == 0) path.moveTo(offset.x, offset.y) else path.lineTo(offset.x, offset.y)
            }

            // Halo cian
            drawPath(
                path = path,
                color = Color(0x4000E5FF),
                style = Stroke(width = 14f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Línea principal azul médico
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF00E5FF), Color(0xFF0056B3)),
                    start = offsets.first(),
                    end = offsets.last()
                ),
                style = Stroke(
                    width = 6f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 10f), 0f)
                )
            )

            // 3. Puntos intermedios / Peajes
            offsets.drop(1).dropLast(1).forEach { pt ->
                drawCircle(color = Color(0xFF00E5FF), radius = 6f, center = pt)
                drawCircle(color = Color.White, radius = 3f, center = pt)
            }

            // 4. Marcador de Origen (Verde)
            val startPt = offsets.first()
            drawCircle(color = Color(0x402E7D32), radius = 18f, center = startPt)
            drawCircle(color = Color(0xFF2E7D32), radius = 10f, center = startPt)
            drawCircle(color = Color.White, radius = 4f, center = startPt)

            // 5. Marcador de Destino Hospitalario (Rojo/Cruz)
            val endPt = offsets.last()
            drawCircle(color = Color(0x40D32F2F), radius = 22f, center = endPt)
            drawCircle(color = Color(0xFFD32F2F), radius = 12f, center = endPt)
            drawCircle(color = Color.White, radius = 5f, center = endPt)

            // Textos descriptivos con Paint nativo
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 34f
                isAntiAlias = true
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            }
            val subTextPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.LTGRAY
                textSize = 26f
                isAntiAlias = true
            }

            drawContext.canvas.nativeCanvas.apply {
                drawText("● Origen: ${traslado.origenNombre}", startPt.x + 22f, startPt.y + 10f, textPaint)
                drawText("✚ Destino: ${traslado.destinoNombre}", endPt.x + 24f, endPt.y + 10f, textPaint)
                drawText("(${traslado.kilometros} km recorridos)", endPt.x + 24f, endPt.y + 44f, subTextPaint)
            }
        }
    }
}

/**
 * Dispara un Intent directo hacia Google Maps, Waze o app de GPS instalada
 * para navegación asistida en ruta real.
 */
private fun launchExternalGpsNavigation(
    context: Context,
    origenLat: Double,
    origenLng: Double,
    destinoLat: Double,
    destinoLng: Double,
    destinoNombre: String
) {
    try {
        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$origenLat,$origenLng&destination=$destinoLat,$destinoLng&travelmode=driving")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback a URI geo genérica
        val geoUri = Uri.parse("geo:$destinoLat,$destinoLng?q=$destinoLat,$destinoLng($destinoNombre)")
        val fallbackIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(fallbackIntent)
    }
}
