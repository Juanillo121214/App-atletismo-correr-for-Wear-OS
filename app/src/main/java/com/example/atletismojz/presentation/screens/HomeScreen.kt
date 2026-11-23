package com.example.atletismojz.presentation.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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

@Composable
fun HomeScreen(
    onStartContinuousRun: () -> Unit,
    onStartManualSeries: () -> Unit,
    onOpenJoggingSetup: () -> Unit,
    onOpenIntervalSetup: () -> Unit,
    onOpenMultiBlockSetup: () -> Unit,
    onOpenDistanceSetup: () -> Unit,
    onOpenHistory: () -> Unit
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
                            text = "Atletismo JZ",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                item {
                    MenuOptionButton(
                        text = "Carrera Continua",
                        icon = Icons.AutoMirrored.Filled.DirectionsRun,
                        onClick = onStartContinuousRun,
                        transformationSpec = transformationSpec,
                        scope = this
                    )
                }

                item { Spacer(modifier = Modifier.height(4.dp)) }

                item {
                    MenuOptionButton(
                        text = "Series Manuales (Botón)",
                        icon = Icons.AutoMirrored.Filled.DirectionsRun,
                        onClick = onStartManualSeries,
                        transformationSpec = transformationSpec,
                        scope = this
                    )
                }

                item { Spacer(modifier = Modifier.height(4.dp)) }

                item {
                    MenuOptionButton(
                        text = "Trote por Tiempo",
                        icon = Icons.AutoMirrored.Filled.DirectionsRun,
                        onClick = onOpenJoggingSetup,
                        transformationSpec = transformationSpec,
                        scope = this
                    )
                }

                item { Spacer(modifier = Modifier.height(4.dp)) }

                item {
                    MenuOptionButton(
                        text = "Cambios 1 Bloque",
                        icon = Icons.Default.Timer,
                        onClick = onOpenIntervalSetup,
                        transformationSpec = transformationSpec,
                        scope = this
                    )
                }

                item { Spacer(modifier = Modifier.height(4.dp)) }

                item {
                    MenuOptionButton(
                        text = "Cambios Multi-Bloque",
                        icon = Icons.Default.Timer,
                        onClick = onOpenMultiBlockSetup,
                        transformationSpec = transformationSpec,
                        scope = this
                    )
                }

                item { Spacer(modifier = Modifier.height(4.dp)) }

                item {
                    MenuOptionButton(
                        text = "Objetivo Distancia",
                        icon = Icons.AutoMirrored.Filled.DirectionsRun,
                        onClick = onOpenDistanceSetup,
                        transformationSpec = transformationSpec,
                        scope = this
                    )
                }

                item { Spacer(modifier = Modifier.height(4.dp)) }

                item {
                    MenuOptionButton(
                        text = "Historial",
                        icon = Icons.Default.History,
                        onClick = onOpenHistory,
                        transformationSpec = transformationSpec,
                        scope = this,
                        isSecondary = true
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuOptionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    transformationSpec: androidx.wear.compose.material3.lazy.TransformationSpec,
    scope: androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope,
    isSecondary: Boolean = false
) {
    val colors = if (isSecondary) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .transformedHeight(scope, transformationSpec),
        transformation = with(scope) { SurfaceTransformation(transformationSpec) },
        colors = colors,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = text
            )
        }
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}
