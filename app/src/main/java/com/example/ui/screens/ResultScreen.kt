package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import com.example.data.model.InspectionProfile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VerificationVerdict
import com.example.ui.components.BiometricsCard
import com.example.ui.components.CircularConfidenceGauge
import com.example.ui.components.DiffVisualizer
import com.example.ui.components.VerdictBadge
import com.example.ui.components.scannerCorners
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ForgeryRed
import com.example.ui.theme.ForgeryRedBg
import com.example.ui.theme.GenuineGreen
import com.example.ui.theme.GenuineGreenBg
import com.example.ui.theme.MidnightCardBg
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.MidnightSurfaceVariant
import com.example.ui.theme.PrimaryCyberGradient
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberBg
import com.example.ui.viewmodel.SignatureViewModel
import com.example.ui.viewmodel.VerificationState

@Composable
fun ResultScreen(
    viewModel: SignatureViewModel,
    onBackToHome: () -> Unit,
    onVerifyAnother: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val verificationState by viewModel.verificationState.collectAsState()
    val sampleBitmap by viewModel.sampleBitmap.collectAsState()
    val referenceBitmap by viewModel.referenceBitmap.collectAsState()

    var showRawResponse by remember { mutableStateOf(false) }

    val completedResult = (verificationState as? VerificationState.Completed)?.result

    if (completedResult == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MidnightDark),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Tidak ada data hasil verifikasi aktif", color = Color(0xFF94A3B8))
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onBackToHome,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = MidnightDark)
                ) {
                    Text("Kembali ke Beranda", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val verdict = completedResult.verdict
    val (verdictColor, verdictBgColor, verdictText) = when (verdict) {
        VerificationVerdict.ASLI -> Triple(GenuineGreen, GenuineGreenBg, "TANDA TANGAN ASLI")
        VerificationVerdict.PALSU -> Triple(ForgeryRed, ForgeryRedBg, "TANDA TANGAN PALSU")
        VerificationVerdict.MENCURIGAKAN -> Triple(WarningAmber, WarningAmberBg, "PERLU ANALISIS ULANG")
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackToHome,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MidnightSurfaceVariant)
                            .border(1.dp, Color(0xFF334155), CircleShape)
                            .testTag("result_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Hasil Analisis Forensik",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Sertifikat Verifikasi Biometrik AI",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyanAccent
                        )
                    }
                }

                IconButton(
                    onClick = {
                        val shareText = """
                            *Sertifikat Verifikasi Tanda Tangan - Signature App*
                            Keputusan: ${completedResult.verdict.labelIndo}
                            Skor Akurasi / Kemiripan: ${completedResult.confidence.toInt()}%
                            Engine Pemroses: ${completedResult.sourceLabel}
                            Waktu Komputasi: ${completedResult.latencyMs} ms
                        """.trimIndent()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Bagikan Hasil Verifikasi"))
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MidnightSurfaceVariant)
                        .border(1.dp, Color(0xFF334155), CircleShape)
                        .testTag("share_result_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Bagikan", tint = CyanAccent)
                }
            }
        }

        // Hero Biometric Certificate Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        1.5.dp,
                        Brush.verticalGradient(listOf(verdictColor.copy(alpha = 0.8f), Color(0x221E293B))),
                        RoundedCornerShape(24.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = MidnightCardBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Circular Radial Gauge
                    CircularConfidenceGauge(
                        score = completedResult.confidence,
                        verdict = completedResult.verdict
                    )

                    Spacer(Modifier.height(18.dp))

                    VerdictBadge(verdict = completedResult.verdict)

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = if (verdict == VerificationVerdict.ASLI) {
                            "Pola stroke dan morfologi tanda tangan selaras dengan spesimen otentik"
                        } else {
                            "Terdeteksi getaran ragu (hesitation tremor) dan deviasi struktur goresan"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = completedResult.sourceLabel,
                                fontSize = 11.sp,
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Latensi: ${completedResult.latencyMs} ms",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Practical Advice Card for General Public & Business Use
        item {
            val activeProfile by viewModel.inspectionProfile.collectAsState()
            val adviceText = remember(verdict, completedResult.confidence, activeProfile) {
                viewModel.getPracticalAdvice(verdict, completedResult.confidence, activeProfile)
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MidnightCardBg),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(listOf(verdictColor.copy(alpha = 0.5f), Color(0x1A1E293B)))
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(verdictColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (verdict == VerificationVerdict.ASLI) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = verdictColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = if (activeProfile == InspectionProfile.GENERAL)
                                "Ulasan & Rekomendasi Pemakaian Sehari-hari"
                            else
                                "Evaluasi Otentisitas Formal & Bisnis",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = adviceText,
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Side-by-Side Visual Inspection Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MidnightCardBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(Color(0x3300E5FF), Color(0x1A1E293B))))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Inspeksi Visual Berkas Tanda Tangan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Sample Preview
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Sampel Uji",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF080D1A))
                                    .scannerCorners(CyanAccent, 14.dp, 1.5.dp)
                                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (sampleBitmap != null) {
                                    Image(
                                        bitmap = sampleBitmap!!.asImageBitmap(),
                                        contentDescription = "Sampel",
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }

                        // Reference Preview
                        if (referenceBitmap != null) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Referensi Sah",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GenuineGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF080D1A))
                                        .scannerCorners(GenuineGreen, 14.dp, 1.5.dp)
                                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = referenceBitmap!!.asImageBitmap(),
                                        contentDescription = "Referensi",
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Difference Map Heatmap
        if (completedResult.diffBitmap != null) {
            item {
                DiffVisualizer(diffBitmap = completedResult.diffBitmap)
            }
        }

        // Biometrics Card
        item {
            BiometricsCard(biometrics = completedResult.biometrics)
        }

        // Raw Response Inspector
        if (completedResult.rawResponse != null && completedResult.rawResponse.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MidnightSurfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showRawResponse = !showRawResponse },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Respon Mentah Model AI (JSON)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                            Icon(
                                imageVector = if (showRawResponse) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8)
                            )
                        }

                        AnimatedVisibility(visible = showRawResponse) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MidnightDark)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = completedResult.rawResponse,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Actions Bottom Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onVerifyAnother,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .background(PrimaryCyberGradient, RoundedCornerShape(16.dp))
                        .testTag("verify_another_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = MidnightDark)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Periksa Tanda Tangan Lain",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = MidnightDark
                        )
                    }
                }

                OutlinedButton(
                    onClick = onBackToHome,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("back_to_dashboard_button")
                ) {
                    Text("Kembali ke Beranda", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
