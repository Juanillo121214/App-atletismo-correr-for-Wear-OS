package com.example.atletismojz.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.example.atletismojz.data.model.LocationPoint
import com.example.atletismojz.data.model.WorkoutSummary
import com.example.atletismojz.data.model.WorkoutType

@Composable
fun WorkoutSummaryScreen(
    summary: WorkoutSummary,
    onDone: () -> Unit
) {
    AppScaffold {
        val listState = rememberTransformingLazyColumnState()
        val transformationSpec = rememberTransformationSpec()

        ScreenScaffold(scrollState = listState) { contentPadding ->
            TransformingLazyColumn(
                contentPadding = contentPadding,
                state = listState
            ) {
                item {
                    ListHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = with(this) { SurfaceTransformation(transformationSpec) }
                    ) {
                        Text(
                            text = "¡Entrenado!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                item {
                    val label = when (summary.workoutType) {
                        WorkoutType.INTERVALS -> {
                            val cfg = summary.intervalConfig
                            "Cambios (${cfg?.workSeconds}s/${cfg?.restSeconds}s x${summary.totalSetsCompleted})"
                        }
                        WorkoutType.MULTI_BLOCK_INTERVALS -> {
                            "Cambios Multi-Bloque (${summary.multiBlockConfig?.blocks?.size ?: 0} Bloques)"
                        }
                        WorkoutType.MANUAL_SERIES -> {
                            "Series Manuales (${summary.manualSplits.size} tramos)"
                        }
                        WorkoutType.JOGGING -> "Trote por Tiempo"
                        WorkoutType.DISTANCE_TARGET -> "Objetivo Distancia"
                        WorkoutType.CONTINUOUS_RUN -> "Carrera Continua"
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                item { Spacer(modifier = Modifier.height(4.dp)) }

                item { SummaryRow("Tiempo Total", summary.formattedDuration) }
                item { SummaryRow("Distancia", summary.formattedDistance) }
                item { SummaryRow("Ritmo Medio", summary.formattedPace) }
                item { SummaryRow("Pulsaciones Med.", if (summary.avgHeartRate > 0) "${summary.avgHeartRate} BPM" else "--") }
                item { SummaryRow("Pulsaciones Máx.", if (summary.maxHeartRate > 0) "${summary.maxHeartRate} BPM" else "--") }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Desglose de Series Manuales
                if (summary.manualSplits.isNotEmpty()) {
                    item {
                        Text(
                            text = "Desglose de Series",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                    items(summary.manualSplits.size) { idx ->
                        val split = summary.manualSplits[idx]
                        val typeText = if (split.isWorkPhase) "Correr" else "Trotar"
                        val phaseColor = if (split.isWorkPhase) Color(0xFF4CAF50) else Color(0xFFFF9800)

                        androidx.compose.foundation.layout.Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .background(MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(8.dp))
                                .padding(6.dp)
                        ) {
                            androidx.compose.foundation.layout.Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "#${split.splitIndex} $typeText",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                    color = phaseColor
                                )
                                Text(
                                    text = split.formattedDuration,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            androidx.compose.foundation.layout.Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = split.formattedDistance,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${split.formattedPace} /km",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }

                if (summary.routePoints.size > 1) {
                    item {
                        Text(
                            text = "Recorrido GPS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    item {
                        RouteCanvasView(points = summary.routePoints)
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }

                item {
                    Button(
                        onClick = onDone,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = with(this) { SurfaceTransformation(transformationSpec) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        icon = {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Aceptar")
                        }
                    ) {
                        Text("Aceptar", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun RouteCanvasView(points: List<LocationPoint>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(Color(0xFF1E1E2C), shape = RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(74.dp)) {
            val minLat = points.minOf { it.latitude }
            val maxLat = points.maxOf { it.latitude }
            val minLng = points.minOf { it.longitude }
            val maxLng = points.maxOf { it.longitude }

            val latRange = if (maxLat - minLat == 0.0) 1.0 else maxLat - minLat
            val lngRange = if (maxLng - minLng == 0.0) 1.0 else maxLng - minLng

            val path = Path()
            points.forEachIndexed { index, pt ->
                val x = ((pt.longitude - minLng) / lngRange * (size.width - 20.dp.toPx()) + 10.dp.toPx()).toFloat()
                val y = (size.height - ((pt.latitude - minLat) / latRange * (size.height - 20.dp.toPx()) + 10.dp.toPx())).toFloat()

                if (index == 0) {
                    path.moveTo(x, y)
                    drawCircle(Color(0xFF4CAF50), radius = 4.dp.toPx(), center = Offset(x, y))
                } else {
                    path.lineTo(x, y)
                }

                if (index == points.lastIndex) {
                    drawCircle(Color(0xFFFF5252), radius = 4.dp.toPx(), center = Offset(x, y))
                }
            }

            drawPath(
                path = path,
                color = Color(0xFF00E5FF),
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}
