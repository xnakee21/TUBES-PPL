package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.MidnightSurfaceVariant

data class PathPoint(val offset: Offset)
data class DrawnPath(val points: List<PathPoint>, val strokeWidth: Float = 6f)

@Composable
fun SignatureCanvas(
    modifier: Modifier = Modifier,
    onSignatureConfirmed: (Bitmap) -> Unit,
    onCancel: () -> Unit
) {
    val paths = remember { mutableStateListOf<DrawnPath>() }
    var currentPathPoints by remember { mutableStateOf<List<PathPoint>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MidnightDark, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Goreskan Tanda Tangan",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        if (paths.isNotEmpty()) paths.removeAt(paths.size - 1)
                    },
                    enabled = paths.isNotEmpty(),
                    modifier = Modifier.testTag("undo_stroke_button")
                ) {
                    Icon(Icons.Default.Undo, contentDescription = "Batal goresan terakhir")
                    Spacer(Modifier.width(4.dp))
                    Text("Undo", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        paths.clear()
                        currentPathPoints = emptyList()
                    },
                    modifier = Modifier.testTag("clear_canvas_button")
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Hapus semua goresan")
                    Spacer(Modifier.width(4.dp))
                    Text("Hapus", fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Drawing Surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F172A))
                .border(1.5.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .onSizeChanged { canvasSize = it }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPathPoints = listOf(PathPoint(offset))
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentPathPoints = currentPathPoints + PathPoint(change.position)
                        },
                        onDragEnd = {
                            if (currentPathPoints.isNotEmpty()) {
                                paths.add(DrawnPath(currentPathPoints))
                                currentPathPoints = emptyList()
                            }
                        },
                        onDragCancel = {
                            currentPathPoints = emptyList()
                        }
                    )
                }
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                // Subtle baseline guide line
                val baselineY = size.height * 0.75f
                drawLine(
                    color = Color(0x3300E5FF),
                    start = Offset(24f, baselineY),
                    end = Offset(size.width - 24f, baselineY),
                    strokeWidth = 2f
                )

                // Render saved paths
                paths.forEach { drawnPath ->
                    if (drawnPath.points.size > 1) {
                        val path = androidx.compose.ui.graphics.Path()
                        path.moveTo(drawnPath.points.first().offset.x, drawnPath.points.first().offset.y)
                        for (i in 1 until drawnPath.points.size) {
                            path.lineTo(drawnPath.points[i].offset.x, drawnPath.points[i].offset.y)
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFF00E5FF),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = drawnPath.strokeWidth,
                                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                join = androidx.compose.ui.graphics.StrokeJoin.Round
                            )
                        )
                    }
                }

                // Render currently active path
                if (currentPathPoints.size > 1) {
                    val path = androidx.compose.ui.graphics.Path()
                    path.moveTo(currentPathPoints.first().offset.x, currentPathPoints.first().offset.y)
                    for (i in 1 until currentPathPoints.size) {
                        path.lineTo(currentPathPoints[i].offset.x, currentPathPoints[i].offset.y)
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFF38BDF8),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 6f,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                            join = androidx.compose.ui.graphics.StrokeJoin.Round
                        )
                    )
                }

                if (paths.isEmpty() && currentPathPoints.isEmpty()) {
                    // Empty state watermark hint
                }
            }

            if (paths.isEmpty() && currentPathPoints.isEmpty()) {
                Text(
                    text = "Gunakan jari atau stylus untuk menandatangani di sini",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("cancel_drawing_button")
            ) {
                Text("Batal")
            }

            Button(
                onClick = {
                    if (canvasSize.width > 0 && canvasSize.height > 0 && paths.isNotEmpty()) {
                        val bitmap = exportPathsToBitmap(paths, canvasSize.width, canvasSize.height)
                        onSignatureConfirmed(bitmap)
                    }
                },
                enabled = paths.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = MidnightDark),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("confirm_signature_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Gunakan Tanda Tangan")
            }
        }
    }
}

private fun exportPathsToBitmap(paths: List<DrawnPath>, width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    // Dark ink on pure white background for ML models and image processing pipelines
    canvas.drawColor(android.graphics.Color.WHITE)

    val paint = Paint().apply {
        color = android.graphics.Color.BLACK
        strokeWidth = 6f
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    paths.forEach { drawnPath ->
        if (drawnPath.points.size > 1) {
            val androidPath = Path()
            androidPath.moveTo(drawnPath.points.first().offset.x, drawnPath.points.first().offset.y)
            for (i in 1 until drawnPath.points.size) {
                androidPath.lineTo(drawnPath.points[i].offset.x, drawnPath.points[i].offset.y)
            }
            canvas.drawPath(androidPath, paint)
        }
    }

    return bitmap
}
