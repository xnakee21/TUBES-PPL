package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanAccentGlow
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GenuineGreen
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.MidnightDarker
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.PrimaryCyberGradient
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    onAnimationFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }
    var statusText by remember { mutableStateOf("MEMUAT SISTEM...") }
    var subStatusText by remember { mutableStateOf("Menginisialisasi modul neural forensik") }

    // Infinite transition for continuous scanner effects
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_fx")

    // Laser scan beam position (0f to 1f)
    val laserPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_phase"
    )

    // Rotating HUD Reticle Angle
    val reticleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "reticle_rot"
    )

    // Pulse wave animation
    val pulseWave by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_wave"
    )

    // Timed progression of boot telemetry
    LaunchedEffect(Unit) {
        // Step 1: Initialize
        statusText = "[01/04] INITIALIZING FORENSIC CORE"
        subStatusText = "Memuat arsitektur Siamese CNN & Edge Biometrics"
        progress.animateTo(0.30f, animationSpec = tween(600, easing = FastOutSlowInEasing))

        delay(150)
        // Step 2: Calibrate stroke detector
        statusText = "[02/04] CALIBRATING STROKE DYNAMICS"
        subStatusText = "Penyelarasan kontur kurvatur & kerapatan piksel tinta"
        progress.animateTo(0.65f, animationSpec = tween(700, easing = FastOutSlowInEasing))

        delay(150)
        // Step 3: Neural verification model ready
        statusText = "[03/04] CONNECTING VERIFICATION ENGINE"
        subStatusText = "Validasi signature biometric matrix selesai"
        progress.animateTo(0.92f, animationSpec = tween(600, easing = FastOutSlowInEasing))

        delay(100)
        // Step 4: System Ready
        statusText = "[04/04] SYSTEM READY • SIGNATURE AI"
        subStatusText = "Semua subsistem forensik aktif dan siap digunakan"
        progress.animateTo(1.0f, animationSpec = tween(350, easing = LinearEasing))

        delay(350)
        onAnimationFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F1E36),
                        MidnightDark,
                        MidnightDarker
                    ),
                    center = Offset(500f, 700f),
                    radius = 1200f
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Allow tapping anywhere to enter instantly
                onAnimationFinished()
            }
            .testTag("splash_screen_container")
    ) {
        // Cyber Grid Backdrop
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacing = 40.dp.toPx()
            val lineColor = Color(0x0A00E5FF)

            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = lineColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
                x += gridSpacing
            }

            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = lineColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += gridSpacing
            }
        }

        // Top Header Info & Skip Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(GenuineGreen)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "BIOMETRIC FORENSIC PROTOCOL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GenuineGreen,
                    letterSpacing = 1.2.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x2200E5FF))
                    .border(1.dp, Color(0x4400E5FF), RoundedCornerShape(20.dp))
                    .clickable { onAnimationFinished() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("skip_splash_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Lewati",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Lewati",
                        tint = CyanAccent,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Center Cyber Biometric Scanner
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Scanner Viewport
            Box(
                modifier = Modifier
                    .size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                // Canvas with animated reticles, pulse rings, and laser beam
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val baseRadius = size.width * 0.42f

                    // Pulsing Outer Aura
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                CyanAccentGlow.copy(alpha = 0.45f * (1.3f - pulseWave)),
                                Color.Transparent
                            ),
                            center = center,
                            radius = baseRadius * pulseWave * 1.3f
                        ),
                        center = center,
                        radius = baseRadius * pulseWave * 1.3f
                    )

                    // Outer Target Circle
                    drawCircle(
                        color = Color(0x3300E5FF),
                        center = center,
                        radius = baseRadius,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Inner Concentric Ring
                    drawCircle(
                        color = Color(0x2200E5FF),
                        center = center,
                        radius = baseRadius * 0.72f,
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // Rotating HUD Reticle Ticks
                    rotate(reticleRotation, pivot = center) {
                        val tickCount = 24
                        val tickInnerRadius = baseRadius - 6.dp.toPx()
                        val tickOuterRadius = baseRadius + 4.dp.toPx()
                        for (i in 0 until tickCount) {
                            val angleRad = (i * (360f / tickCount)) * (Math.PI / 180.0)
                            val isMajor = i % 6 == 0
                            val actualInner = if (isMajor) tickInnerRadius - 4.dp.toPx() else tickInnerRadius
                            val startX = (center.x + actualInner * cos(angleRad)).toFloat()
                            val startY = (center.y + actualInner * sin(angleRad)).toFloat()
                            val endX = (center.x + tickOuterRadius * cos(angleRad)).toFloat()
                            val endY = (center.y + tickOuterRadius * sin(angleRad)).toFloat()

                            drawLine(
                                color = if (isMajor) CyanAccent else Color(0x5500E5FF),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                            )
                        }
                    }

                    // 4 Corner HUD Brackets
                    val bracketLen = 22.dp.toPx()
                    val bracketOffset = 18.dp.toPx()
                    val bStroke = 2.5.dp.toPx()

                    // Top Left
                    drawLine(CyanAccent, Offset(bracketOffset, bracketOffset), Offset(bracketOffset + bracketLen, bracketOffset), bStroke)
                    drawLine(CyanAccent, Offset(bracketOffset, bracketOffset), Offset(bracketOffset, bracketOffset + bracketLen), bStroke)

                    // Top Right
                    drawLine(CyanAccent, Offset(size.width - bracketOffset, bracketOffset), Offset(size.width - bracketOffset - bracketLen, bracketOffset), bStroke)
                    drawLine(CyanAccent, Offset(size.width - bracketOffset, bracketOffset), Offset(size.width - bracketOffset, bracketOffset + bracketLen), bStroke)

                    // Bottom Left
                    drawLine(CyanAccent, Offset(bracketOffset, size.height - bracketOffset), Offset(bracketOffset + bracketLen, size.height - bracketOffset), bStroke)
                    drawLine(CyanAccent, Offset(bracketOffset, size.height - bracketOffset), Offset(bracketOffset, size.height - bracketOffset - bracketLen), bStroke)

                    // Bottom Right
                    drawLine(CyanAccent, Offset(size.width - bracketOffset, size.height - bracketOffset), Offset(size.width - bracketOffset - bracketLen, size.height - bracketOffset), bStroke)
                    drawLine(CyanAccent, Offset(size.width - bracketOffset, size.height - bracketOffset), Offset(size.width - bracketOffset, size.height - bracketOffset - bracketLen), bStroke)

                    // Vertical Laser Scan Beam Sweep
                    val laserY = size.height * (0.15f + laserPhase * 0.70f)
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                CyanAccent.copy(alpha = 0.3f),
                                Color.White,
                                CyanAccent.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        start = Offset(bracketOffset, laserY),
                        end = Offset(size.width - bracketOffset, laserY),
                        strokeWidth = 2.5.dp.toPx()
                    )

                    // Laser Glow Trail
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                CyanAccent.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            startY = laserY - 20.dp.toPx(),
                            endY = laserY + 20.dp.toPx()
                        ),
                        topLeft = Offset(bracketOffset, laserY - 20.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(size.width - bracketOffset * 2, 40.dp.toPx())
                    )
                }

                // Center Signature & Forensic Glyph
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(CyanAccent.copy(alpha = 0.15f))
                            .border(1.5.dp, CyanAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "NEURAL SCAN",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = CyanAccent,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            // App Brand Name
            Text(
                text = "Signature",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Text(
                text = "AI BIOMETRIC VERIFICATION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyanAccent,
                letterSpacing = 3.sp
            )

            Spacer(Modifier.height(28.dp))

            // Progress Bar Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Percentage & status indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${(progress.value * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = CyanAccent,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Progress Bar Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MidnightSurface)
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progress.value)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(PrimaryCyberGradient)
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = subStatusText,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Bottom Footer Telemetry
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "SENTUH LAYAR UNTUK MASUK • SIGNATURE v1.0",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B),
                letterSpacing = 1.sp
            )
        }
    }
}
