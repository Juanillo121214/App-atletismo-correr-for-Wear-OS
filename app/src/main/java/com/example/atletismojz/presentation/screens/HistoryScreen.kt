package com.example.atletismojz.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.atletismojz.data.model.WorkoutSummary
import com.example.atletismojz.data.model.WorkoutType
import com.example.atletismojz.data.repository.WorkoutRepository

@Composable
fun HistoryScreen(
    onSelectWorkout: (WorkoutSummary) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { WorkoutRepository(context) }
    var historyList by remember { mutableStateOf<List<WorkoutSummary>>(emptyList()) }

    LaunchedEffect(Unit) {
        historyList = repository.getAllWorkouts()
    }

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
                            text = "Historial",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (historyList.isEmpty()) {
                    item {
                        Text(
                            text = "Sin entrenamientos guardados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(historyList.size) { index ->
                        val summary = historyList[index]
                        WorkoutHistoryItem(
                            summary = summary,
                            onClick = { onSelectWorkout(summary) }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    item { Spacer(modifier = Modifier.height(6.dp)) }

                    item {
                        Button(
                            onClick = {
                                repository.clearAllWorkouts()
                                historyList = emptyList()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            transformation = with(this) { SurfaceTransformation(transformationSpec) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            icon = {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Borrar")
                            }
                        ) {
                            Text("Borrar Historial", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(6.dp)) }

                item {
                    Button(
                        onClick = onBack,
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
                        Text("Volver", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutHistoryItem(
    summary: WorkoutSummary,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = summary.formattedDate,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val typeText = when (summary.workoutType) {
                WorkoutType.INTERVALS -> "Cambios"
                WorkoutType.MULTI_BLOCK_INTERVALS -> "Multi-Bloque"
                WorkoutType.MANUAL_SERIES -> "Series"
                WorkoutType.JOGGING -> "Trote"
                WorkoutType.DISTANCE_TARGET -> "Objetivo"
                WorkoutType.CONTINUOUS_RUN -> "Continua"
            }
            Text(
                text = typeText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${summary.formattedDistance} | ${summary.formattedDuration}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (summary.avgHeartRate > 0) "${summary.avgHeartRate} BPM" else "--",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFF5252)
            )
        }
    }
}
