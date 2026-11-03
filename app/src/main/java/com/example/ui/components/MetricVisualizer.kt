package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SignatureBiometrics
import com.example.data.model.VerificationVerdict
import com.example.ui.theme.CardGlassGradient
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanAccentGlow
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ForgeryRed
import com.example.ui.theme.ForgeryRedBg
import com.example.ui.theme.ForgeryRedGlow
import com.example.ui.theme.GenuineGreen
import com.example.ui.theme.GenuineGreenBg
import com.example.ui.theme.GenuineGreenGlow
import com.example.ui.theme.MidnightCardBg
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.MidnightSurfaceVariant
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberBg
import kotlin.math.roundToInt

fun Modifier.scannerCorners(
    color: Color = CyanAccent,
    length: Dp = 16.dp,
    stroke: Dp = 2.dp
): Modifier = this.drawBehind {
    val strokePx = stroke.toPx()
    val lengthPx = length.toPx()
    val w = size.width
    val h = size.height

    // Top Left
    drawLine(color, Offset(0f, 0f), Offset(lengthPx, 0f), strokePx)
    drawLine(color, Offset(0f, 0f), Offset(0f, lengthPx), strokePx)

    // Top Right
    drawLine(color, Offset(w, 0f), Offset(w - lengthPx, 0f), strokePx)
    drawLine(color, Offset(w, 0f), Offset(w, lengthPx), strokePx)

    // Bottom Left
    drawLine(color, Offset(0f, h), Offset(lengthPx, h), strokePx)
    drawLine(color, Offset(0f, h), Offset(0f, h - lengthPx), strokePx)

    // Bottom Right
    drawLine(color, Offset(w, h), Offset(w - lengthPx, h), strokePx)
    drawLine(color, Offset(w, h), Offset(w, h - lengthPx), strokePx)
}

@Composable
fun BiometricScanline(
    modifier: Modifier = Modifier,
    scanlineColor: Color = CyanAccent
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanline_trans")
    val offsetYRatio by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanline_y"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.04f)
                .align(Alignment.TopStart)
                .offset(y = 160.dp * offsetYRatio)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            scanlineColor.copy(alpha = 0.8f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun CircularConfidenceGauge(
    score: Float,
    verdict: VerificationVerdict,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateFloatAsState(
        targetValue = score.coerceIn(0f, 100f),
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "score_counter"
    )

    val (glowColor, trackBrush) = when (verdict) {
        VerificationVerdict.ASLI -> Pair(
            GenuineGreenGlow,
            Brush.sweepGradient(listOf(CyanAccent, GenuineGreen, CyanAccent))
        )
        VerificationVerdict.PALSU -> Pair(
            ForgeryRedGlow,
            Brush.sweepGradient(listOf(Color(0xFFFF5252), ForgeryRed, Color(0xFFFF7B00)))
        )
        VerificationVerdict.MENCURIGAKAN -> Pair(
            WarningAmberBg,
            Brush.sweepGradient(listOf(WarningAmber, Color(0xFFFBBF24), WarningAmber))
        )
    }

    Box(
        modifier = modifier.size(190.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer ambient glow ring
        Box(
            modifier = Modifier
                .size(175.dp)
                .clip(CircleShape)
                .background(glowColor)
        )

        // Radial gauge Canvas
        androidx.compose.foundation.Canvas(modifier = Modifier.size(160.dp)) {
            val strokeWidth = 12.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val arcTopLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            val arcSize = Size(diameter, diameter)

            // Background Track
            drawArc(
                color = Color(0x331E293B),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Foreground Progress Arc
            val sweepProgress = (animatedScore / 100f) * 270f
            drawArc(
                brush = trackBrush,
                startAngle = 135f,
                sweepAngle = sweepProgress,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Center Content
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = CyanAccent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${animatedScore.roundToInt()}",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            Text(
                text = "SKOR AKURASI",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun VerdictBadge(
    verdict: VerificationVerdict,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, textColor, icon, label) = when (verdict) {
        VerificationVerdict.ASLI -> Quintuple(
            GenuineGreenBg,
            GenuineGreen,
            GenuineGreen,
            Icons.Default.CheckCircle,
            "TANDA TANGAN ASLI"
        )
        VerificationVerdict.PALSU -> Quintuple(
            ForgeryRedBg,
            ForgeryRed,
            ForgeryRed,
            Icons.Default.Dangerous,
            "TANDA TANGAN PALSU"
        )
        VerificationVerdict.MENCURIGAKAN -> Quintuple(
            WarningAmberBg,
            WarningAmber,
            WarningAmber,
            Icons.Default.Help,
            "PERLU ANALISIS ULANG"
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(32.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
fun MetricBar(
    label: String,
    value: Float,
    displayValue: String = "${value.toInt()}%",
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (value / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "metric_anim"
    )

    val (barGradient, statusChip) = when {
        value >= 75f -> Pair(
            Brush.horizontalGradient(listOf(CyanAccent, GenuineGreen)),
            "Optimal"
        )
        value >= 50f -> Pair(
            Brush.horizontalGradient(listOf(WarningAmber, Color(0xFFFCD34D))),
            "Waspada"
        )
        else -> Pair(
            Brush.horizontalGradient(listOf(Color(0xFFFF3366), ForgeryRed)),
            "Deviasi"
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (value >= 70) GenuineGreen else ForgeryRed)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE2E8F0),
                    fontSize = 13.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x331E293B))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = statusChip,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (value >= 70) GenuineGreen else WarningAmber
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = displayValue,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1E293B))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(barGradient)
            )
        }
    }
}

@Composable
fun BiometricsCard(
    biometrics: SignatureBiometrics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightCardBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(Color(0x4000E5FF), Color(0x1A1E293B))))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Analisis Parameter Biometrik",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = "6 Fitur Morfologi",
                    fontSize = 11.sp,
                    color = CyanAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }

            MetricBar(
                label = "Kontinuitas & Kecepatan Goresan",
                value = biometrics.strokeContinuity
            )

            MetricBar(
                label = "Kesesuaian Rasio Geometris",
                value = biometrics.aspectProportionMatch
            )

            MetricBar(
                label = "Distribusi Kerapatan Tinta",
                value = biometrics.pixelDensityConsistency
            )

            MetricBar(
                label = "Kemiripan Struktural (SSIM/Jaccard)",
                value = biometrics.structuralSimilarity
            )

            MetricBar(
                label = "Kelenturan (Smoothness vs Tremor)",
                value = biometrics.smoothnessScore
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kemiringan Garis Dasar (Baseline):",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "${biometrics.baselineSlope}°",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                )
            }
        }
    }
}

@Composable
fun DiffVisualizer(
    diffBitmap: Bitmap,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightCardBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(Color(0x3300E5FF), Color(0x1A1E293B))))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Peta Deviasi Visual (Diff Heatmap)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x3300E5FF))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Superimpose", fontSize = 10.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .size(210.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0A0F1D))
                    .scannerCorners(CyanAccent, 20.dp, 2.dp)
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = diffBitmap.asImageBitmap(),
                    contentDescription = "Peta perbedaan tanda tangan",
                    modifier = Modifier.size(190.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            // Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = CyanAccent, label = "Cocok (Asli)")
                LegendItem(color = ForgeryRed, label = "Deviasi (Palsu)")
                LegendItem(color = Color(0xFF3B82F6), label = "Referensi")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFCBD5E1)
        )
    }
}

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
