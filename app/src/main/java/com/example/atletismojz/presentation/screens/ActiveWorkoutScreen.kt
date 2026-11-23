package com.example.atletismojz.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.example.atletismojz.data.model.WorkoutData
import com.example.atletismojz.data.model.WorkoutState
import com.example.atletismojz.data.model.WorkoutType
import kotlinx.coroutines.launch

@Composable
fun ActiveWorkoutScreen(
    workoutData: WorkoutData,
    onPauseWorkout: () -> Unit,
    onResumeWorkout: () -> Unit,
    onStopWorkout: () -> Unit,
    onAddLap: () -> Unit,
    onBackToHome: () -> Unit
) {
    val listState = rememberTransformingLazyColumnState()
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AppScaffold {
        val transformationSpec = rememberTransformationSpec()

        ScreenScaffold(scrollState = listState) { contentPadding ->
            TransformingLazyColumn(
                contentPadding = contentPadding,
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester)
                    .focusable()
                    .onRotaryScrollEvent { event ->
                        coroutineScope.launch {
                            listState.scrollBy(event.verticalScrollPixels)
                        }
                        true
                    }
            ) {
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Tarjeta Principal Interactiva (Tocar aquí cambia Correr ⇄ Trotar al 1er toque)
                item {
                    val isManual = workoutData.type == WorkoutType.MANUAL_SERIES
                    val cardContainerColor = if (isManual) {
                        if (workoutData.isWorkPhase) Color(0xFF1B5E20) else Color(0xFFE65100)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    }

                    Card(
                        onClick = onAddLap,
                        modifier = Modifier.fillMaxWidth(0.95f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = cardContainerColor
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            when (workoutData.type) {
                                WorkoutType.MANUAL_SERIES -> {
                                    val phaseLabel = if (workoutData.isWorkPhase) "¡CORRER!" else "DESCANSO"
                                    val splitNum = workoutData.manualSplits.size + 1

                                    Text(
                                        text = "#$splitNum $phaseLabel",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = workoutData.formattedSplitDuration,
                                        style = MaterialTheme.typography.displayMedium.copy(
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "¡Toca aquí para cambiar!",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                                WorkoutType.MULTI_BLOCK_INTERVALS -> {
                                    val phaseLabel = if (workoutData.isWorkPhase) "¡CORRER!" else "TROTAR"
                                    val blockNum = workoutData.currentBlockIndex + 1
                                    val totalBlocks = workoutData.multiBlockConfig.blocks.size
                                    val currentBlock = workoutData.multiBlockConfig.blocks.getOrNull(workoutData.currentBlockIndex)

                                    Text(
                                        text = "B$blockNum/$totalBlocks $phaseLabel (${workoutData.currentSet}/${currentBlock?.totalSets ?: 1})",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = workoutData.formattedDuration,
                                        style = MaterialTheme.typography.displayMedium.copy(
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = Color.White
                                    )
                                }
                                else -> {
                                    Text(
                                        text = workoutData.formattedDuration,
                                        style = MaterialTheme.typography.displayMedium.copy(
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(6.dp)) }

                // Datos: Distancia, Pulsaciones (con Zona HR) y Ritmo
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = workoutData.formattedDistanceKm,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Distancia",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val hrZone = workoutData.heartRateZone
                        val zoneColor = Color(hrZone.colorHex)

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Pulsaciones",
                                    tint = zoneColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = if (workoutData.currentHeartRate > 0) "${workoutData.currentHeartRate}" else "--",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = zoneColor
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(zoneColor.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "${hrZone.name} ${hrZone.description}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = zoneColor
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = workoutData.formattedPace,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "/km",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Botones de Control
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (workoutData.state == WorkoutState.RUNNING) {
                            IconButton(
                                onClick = onPauseWorkout,
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Pause, contentDescription = "Pausar")
                            }
                        } else {
                            IconButton(
                                onClick = onResumeWorkout,
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = Color(0xFF4CAF50)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Reanudar",
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onAddLap,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = "Marca",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onStopWorkout,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Detener",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(10.dp)) }

                item {
                    Button(
                        onClick = onBackToHome,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = with(this) { SurfaceTransformation(transformationSpec) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        icon = {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    ) {
                        Text("Volver al Menú", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
