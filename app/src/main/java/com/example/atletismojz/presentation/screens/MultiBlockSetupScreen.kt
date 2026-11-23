package com.example.atletismojz.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.atletismojz.data.model.IntervalBlock
import com.example.atletismojz.data.model.MultiBlockIntervalConfig
import kotlinx.coroutines.launch

@Composable
fun MultiBlockSetupScreen(
    onStartMultiBlockWorkout: (MultiBlockIntervalConfig) -> Unit,
    onBack: () -> Unit
) {
    var blockCount by remember { mutableIntStateOf(2) } // 1 a 3 bloques

    var b1Work by remember { mutableIntStateOf(30) }
    var b1Rest by remember { mutableIntStateOf(30) }
    var b1Sets by remember { mutableIntStateOf(5) }

    var b2Work by remember { mutableIntStateOf(45) }
    var b2Rest by remember { mutableIntStateOf(45) }
    var b2Sets by remember { mutableIntStateOf(4) }

    var b3Work by remember { mutableIntStateOf(60) }
    var b3Rest by remember { mutableIntStateOf(30) }
    var b3Sets by remember { mutableIntStateOf(3) }

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
                    ListHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = with(this) { SurfaceTransformation(transformationSpec) }
                    ) {
                        Text(
                            text = "Bloques Cambios",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Cantidad de bloques
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nº Bloques",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (blockCount > 1) blockCount -= 1 },
                                modifier = Modifier.width(36.dp).height(36.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = "Menos")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$blockCount",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { if (blockCount < 3) blockCount += 1 },
                                modifier = Modifier.width(36.dp).height(36.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Más")
                            }
                        }
                    }
                }

                // Bloque 1
                item {
                    BlockConfigCard(
                        blockTitle = "Bloque 1",
                        workSec = b1Work,
                        restSec = b1Rest,
                        sets = b1Sets,
                        onWorkChange = { b1Work = it },
                        onRestChange = { b1Rest = it },
                        onSetsChange = { b1Sets = it }
                    )
                }

                // Bloque 2
                if (blockCount >= 2) {
                    item {
                        BlockConfigCard(
                            blockTitle = "Bloque 2",
                            workSec = b2Work,
                            restSec = b2Rest,
                            sets = b2Sets,
                            onWorkChange = { b2Work = it },
                            onRestChange = { b2Rest = it },
                            onSetsChange = { b2Sets = it }
                        )
                    }
                }

                // Bloque 3
                if (blockCount >= 3) {
                    item {
                        BlockConfigCard(
                            blockTitle = "Bloque 3",
                            workSec = b3Work,
                            restSec = b3Rest,
                            sets = b3Sets,
                            onWorkChange = { b3Work = it },
                            onRestChange = { b3Rest = it },
                            onSetsChange = { b3Sets = it }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    Button(
                        onClick = {
                            val blocks = mutableListOf(
                                IntervalBlock(b1Work, b1Rest, b1Sets)
                            )
                            if (blockCount >= 2) blocks.add(IntervalBlock(b2Work, b2Rest, b2Sets))
                            if (blockCount >= 3) blocks.add(IntervalBlock(b3Work, b3Rest, b3Sets))

                            onStartMultiBlockWorkout(MultiBlockIntervalConfig(blocks))
                        },
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
                        Text("¡Iniciar Multi-Bloque!", style = MaterialTheme.typography.labelLarge)
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

@Composable
private fun BlockConfigCard(
    blockTitle: String,
    workSec: Int,
    restSec: Int,
    sets: Int,
    onWorkChange: (Int) -> Unit,
    onRestChange: (Int) -> Unit,
    onSetsChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Text(
            text = blockTitle,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        SelectorRow("Correr", "${workSec}s", { if (workSec > 10) onWorkChange(workSec - 5) }, { if (workSec < 300) onWorkChange(workSec + 5) })
        SelectorRow("Trotar", "${restSec}s", { if (restSec > 5) onRestChange(restSec - 5) }, { if (restSec < 300) onRestChange(restSec + 5) })
        SelectorRow("Series", "$sets", { if (sets > 1) onSetsChange(sets - 1) }, { if (sets < 50) onSetsChange(sets + 1) })
    }
}

@Composable
private fun SelectorRow(
    label: String,
    value: String,
    onDec: () -> Unit,
    onInc: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDec, modifier = Modifier.width(32.dp).height(32.dp)) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "Menos")
            }
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.width(2.dp))
            IconButton(onClick = onInc, modifier = Modifier.width(32.dp).height(32.dp)) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Más")
            }
        }
    }
}
