package com.example.atletismojz.presentation.screens

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight

private val ATHLETICS_DISTANCES_METERS = buildList {
    for (m in 10..500 step 10) {
        add(m)
    }
    addAll(listOf(600, 800, 1000, 1500, 2000, 3000, 5000, 10000))
}

@Composable
fun DistanceSetupScreen(
    onStartDistanceWorkout: (targetMeters: Double) -> Unit,
    onBack: () -> Unit
) {
    var selectedIndex by remember { mutableIntStateOf(ATHLETICS_DISTANCES_METERS.indexOf(400).coerceAtLeast(0)) }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val currentMeters = ATHLETICS_DISTANCES_METERS[selectedIndex]
    val displayText = if (currentMeters < 1000) {
        "$currentMeters m"
    } else {
        val km = currentMeters / 1000.0
        if (km % 1.0 == 0.0) "${km.toInt()} km" else "$km km"
    }

    AppScaffold {
        val listState = rememberTransformingLazyColumnState()
        val transformationSpec = rememberTransformationSpec()

        ScreenScaffold(scrollState = listState) { contentPadding ->
            TransformingLazyColumn(
                contentPadding = contentPadding,
                state = listState,
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .focusable()
                    .onRotaryScrollEvent { event ->
                        if (event.verticalScrollPixels < 0) {
                            if (selectedIndex > 0) selectedIndex -= 1
                            true
                        } else if (event.verticalScrollPixels > 0) {
                            if (selectedIndex < ATHLETICS_DISTANCES_METERS.lastIndex) selectedIndex += 1
                            true
                        } else {
                            false
                        }
                    }
            ) {
                item {
                    ListHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = with(this) { SurfaceTransformation(transformationSpec) }
                    ) {
                        Text(
                            text = "Series y Distancia",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gira corona",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (selectedIndex > 0) selectedIndex -= 1 },
                                modifier = Modifier.width(36.dp).height(36.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = "Menos")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = displayText,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { if (selectedIndex < ATHLETICS_DISTANCES_METERS.lastIndex) selectedIndex += 1 },
                                modifier = Modifier.width(36.dp).height(36.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Más")
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    Button(
                        onClick = { onStartDistanceWorkout(currentMeters.toDouble()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = with(this) { SurfaceTransformation(transformationSpec) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        icon = {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Iniciar")
                        }
                    ) {
                        Text("¡Iniciar $displayText!", style = MaterialTheme.typography.labelLarge)
                    }
                }

                item { Spacer(modifier = Modifier.height(4.dp)) }

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
