package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.GeneralUseCase
import com.example.data.model.InspectionProfile
import com.example.data.model.VerificationRecord
import com.example.data.model.VerificationVerdict
import com.example.ui.components.VerdictBadge
import com.example.ui.components.scannerCorners
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanAccentGlow
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ForgeryRed
import com.example.ui.theme.GenuineGreen
import com.example.ui.theme.MidnightCardBg
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.MidnightSurfaceVariant
import com.example.ui.theme.PrimaryCyberGradient
import com.example.ui.viewmodel.SignatureViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: SignatureViewModel,
    onNavigateToVerify: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSpecimens: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSelectRecord: (VerificationRecord) -> Unit,
    onReplaySplash: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val records by viewModel.allRecords.collectAsState()
    val activeProfile by viewModel.inspectionProfile.collectAsState()
    var showTipsDialog by remember { mutableStateOf(false) }

    val totalCount = records.size
    val genuineCount = records.count { it.verdict == VerificationVerdict.ASLI }
    val forgedCount = records.count { it.verdict == VerificationVerdict.PALSU }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(CyanAccent.copy(alpha = 0.15f))
                            .border(1.5.dp, CyanAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Signature",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyanAccent.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "UMUM & PROFESIONAL",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }
                        Text(
                            text = "Deteksi Keaslian & Brankas Tanda Tangan",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Educational Guide Button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), CircleShape)
                            .clickable { showTipsDialog = true }
                            .testTag("home_tips_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Panduan & Tips",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Replay Scanner Animation button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CyanAccent.copy(alpha = 0.12f))
                            .border(1.dp, CyanAccent.copy(alpha = 0.35f), CircleShape)
                            .clickable { onReplaySplash() }
                            .testTag("home_replay_splash_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Putar Animasi Pembuka",
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MidnightSurfaceVariant)
                            .border(1.dp, Color(0xFF334155), CircleShape)
                            .clickable { onNavigateToSettings() }
                            .testTag("home_settings_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Mode Profile Switcher Card (Umum vs Formal)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(Color(0x3300E5FF), Color(0x1A1E293B), Color(0x3300E5FF))),
                        RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = MidnightCardBg)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "PROFIL PENGGUNAAN AKTIF",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                letterSpacing = 0.6.sp
                            )
                        }

                        Text(
                            text = if (activeProfile == InspectionProfile.GENERAL) "Toleransi 68%" else "Toleransi 78%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeProfile == InspectionProfile.GENERAL) GenuineGreen else Color(0xFF38BDF8)
                        )
                    }

                    // Toggle Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MidnightSurface)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val isGeneral = activeProfile == InspectionProfile.GENERAL
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isGeneral) CyanAccent.copy(alpha = 0.2f) else Color.Transparent)
                                .border(
                                    if (isGeneral) 1.dp else 0.dp,
                                    if (isGeneral) CyanAccent else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.setInspectionProfile(InspectionProfile.GENERAL) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🌟 Mode Umum (Harian)",
                                fontSize = 12.sp,
                                fontWeight = if (isGeneral) FontWeight.Bold else FontWeight.Medium,
                                color = if (isGeneral) Color.White else Color(0xFF94A3B8)
                            )
                        }

                        val isFormal = activeProfile == InspectionProfile.PROFESSIONAL
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isFormal) Color(0x3338BDF8) else Color.Transparent)
                                .border(
                                    if (isFormal) 1.dp else 0.dp,
                                    if (isFormal) Color(0xFF38BDF8) else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.setInspectionProfile(InspectionProfile.PROFESSIONAL) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⚖️ Mode Formal (Bisnis)",
                                fontSize = 12.sp,
                                fontWeight = if (isFormal) FontWeight.Bold else FontWeight.Medium,
                                color = if (isFormal) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }

                    Text(
                        text = if (activeProfile == InspectionProfile.GENERAL)
                            "Cocok untuk kuitansi belanja/sewa, surat izin sekolah/kantor, tanda terima paket, dan cek konsistensi tanda tangan KTP pribadi."
                        else
                            "Cocok untuk kontrak kerja sama, perbankan/cek giro, akta notaris, dan audit dokumen legal berisiko tinggi.",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Hero Banner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .scannerCorners(CyanAccent, 18.dp, 2.dp)
                    .border(
                        1.dp,
                        Brush.verticalGradient(listOf(Color(0x6600E5FF), Color(0x1A1E293B))),
                        RoundedCornerShape(24.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = MidnightSurface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.signature_hero_banner_1793510609182),
                        contentDescription = "Biometric Signature Verification",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Top-right Futuristic HUD Tag
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(14.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC080D1A))
                            .border(1.dp, Color(0x4400E5FF), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SIAMESE NEURAL • 6 PARAMETER",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            letterSpacing = 0.8.sp
                        )
                    }

                    // Gradient Scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x22000000),
                                        Color(0x99080D1A),
                                        Color(0xF5080D1A)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(GenuineGreen)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Sistem Verifikasi Biometrik Online",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GenuineGreen
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Analisis Otentisitas Goresan Tinta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Deteksi pemalsuan, getaran goresan, rasio aspek, & integritas goresan",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }
        }

        // Primary Action Button (Gradient CTA with Glowing Border)
        item {
            Button(
                onClick = onNavigateToVerify,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .background(PrimaryCyberGradient, RoundedCornerShape(18.dp))
                    .border(
                        1.5.dp,
                        Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.5f), CyanAccent, Color.Transparent)),
                        RoundedCornerShape(18.dp)
                    )
                    .testTag("start_verification_fab")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MidnightDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Mulai Verifikasi Tanda Tangan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = MidnightDark,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Everyday Use-Cases for General Public (Kebutuhan Sehari-hari)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kasus Penggunaan Sehari-hari",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Pilih Skenario",
                        fontSize = 11.sp,
                        color = CyanAccent
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    UseCaseCard(
                        title = "Kuitansi Belanja",
                        subtitle = "Nota pembayaran & sewa",
                        icon = Icons.Default.ReceiptLong,
                        accentColor = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.loadGeneralUseCase(GeneralUseCase.RECEIPT)
                            onNavigateToVerify()
                        }
                    )
                    UseCaseCard(
                        title = "Cek Tangan KTP",
                        subtitle = "Uji sebelum ke teller bank",
                        icon = Icons.Default.Badge,
                        accentColor = GenuineGreen,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.loadGeneralUseCase(GeneralUseCase.ID_CARD)
                            onNavigateToVerify()
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    UseCaseCard(
                        title = "Kontrak / Sewa",
                        subtitle = "Perjanjian kos & kerja",
                        icon = Icons.Default.Description,
                        accentColor = Color(0xFFA855F7),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.loadGeneralUseCase(GeneralUseCase.CONTRACT)
                            onNavigateToVerify()
                        }
                    )
                    UseCaseCard(
                        title = "Paket Ekspedisi",
                        subtitle = "Tanda terima kurir barang",
                        icon = Icons.Default.LocalShipping,
                        accentColor = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.loadGeneralUseCase(GeneralUseCase.PACKAGE)
                            onNavigateToVerify()
                        }
                    )
                }
            }
        }

        // Statistics Overview Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Diperiksa",
                    value = "$totalCount",
                    color = CyanAccent,
                    icon = Icons.Default.Security,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Asli (Genuine)",
                    value = "$genuineCount",
                    color = GenuineGreen,
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Palsu (Forged)",
                    value = "$forgedCount",
                    color = ForgeryRed,
                    icon = Icons.Default.Dangerous,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Tools Row (Expanded to 4 options including Tips & Edukasi)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Akses Cepat & Bantuan",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickToolButton(
                        icon = Icons.Default.PlayArrow,
                        label = "Demo Asli",
                        subtitle = "Spesimen Sah",
                        accentColor = GenuineGreen,
                        onClick = {
                            viewModel.loadPresetDemoSignatures(asli = true)
                            onNavigateToVerify()
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "demo_asli_button"
                    )
                    QuickToolButton(
                        icon = Icons.Default.Dangerous,
                        label = "Demo Palsu",
                        subtitle = "Deteksi Tremor",
                        accentColor = ForgeryRed,
                        onClick = {
                            viewModel.loadPresetDemoSignatures(asli = false)
                            onNavigateToVerify()
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "demo_palsu_button"
                    )
                    QuickToolButton(
                        icon = Icons.Default.Folder,
                        label = "Brankas KTP",
                        subtitle = "Koleksi Saya",
                        accentColor = CyanAccent,
                        onClick = onNavigateToSpecimens,
                        modifier = Modifier.weight(1f),
                        testTag = "specimen_bank_button"
                    )
                    QuickToolButton(
                        icon = Icons.Default.Lightbulb,
                        label = "Tips Aman",
                        subtitle = "Panduan Bank",
                        accentColor = Color(0xFFFBBF24),
                        onClick = { showTipsDialog = true },
                        modifier = Modifier.weight(1f),
                        testTag = "tips_panduan_button"
                    )
                }
            }
        }

        // Recent Verifications Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Riwayat Pemeriksaan Terkini",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (records.isNotEmpty()) {
                    Text(
                        text = "Lihat Semua",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanAccent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onNavigateToHistory() }
                            .padding(4.dp)
                    )
                }
            }
        }

        if (records.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MidnightCardBg)
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(18.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Belum Ada Riwayat Pemeriksaan",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Mulai uji tanda tangan sampel terhadap spesimen resmi",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(records.take(5)) { record ->
                HistoryItemCard(record = record, onClick = { onSelectRecord(record) })
            }
        }
    }

    if (showTipsDialog) {
        SignatureTipsSheet(onDismiss = { showTipsDialog = false })
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightCardBg),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(listOf(color.copy(alpha = 0.45f), Color(0x1A1E293B)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8)
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(color.copy(alpha = 0.6f))
            )
        }
    }
}

@Composable
fun QuickToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    subtitle: String? = null,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightCardBg),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(listOf(accentColor.copy(alpha = 0.35f), Color(0x1A1E293B)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 9.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    record: VerificationRecord,
    onClick: () -> Unit
) {
    val dateStr = remember(record.timestamp) {
        SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID")).format(Date(record.timestamp))
    }

    val isAsli = record.verdict == VerificationVerdict.ASLI
    val statusColor = if (isAsli) GenuineGreen else ForgeryRed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("history_item_${record.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightCardBg),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(statusColor.copy(alpha = 0.35f), Color(0x0D1E293B))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Monogram Avatar + Title & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.5.dp, statusColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = record.signerName.trim().take(1).uppercase().ifBlank { "S" }
                    Text(
                        text = initial,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = statusColor
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.documentTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${record.signerName} • $dateStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Divider subtle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF1E293B))
            )

            // Bottom row: Metric tag & Engine + Verdict
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyanAccent.copy(alpha = 0.12f))
                            .border(1.dp, CyanAccent.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Kecocokan: ${record.confidence.toInt()}%",
                            color = CyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MidnightSurfaceVariant)
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = record.apiSource,
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                VerdictBadge(verdict = record.verdict)
            }
        }
    }
}

@Composable
fun UseCaseCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(
                1.dp,
                Brush.verticalGradient(listOf(accentColor.copy(alpha = 0.4f), Color(0x1A1E293B))),
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = MidnightCardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignatureTipsSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MidnightSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFBBF24).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Panduan & Tips Tanda Tangan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Edukasi praktis untuk keperluan harian & perbankan",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                }
            }

            // Tip 1: Bank rejection
            TipItem(
                number = "1",
                title = "Mengapa Tanda Tangan Sering Ditolak Bank?",
                description = "Teller bank memeriksa tarikan awal (lead-in) dan sudut kemiringan. Jika Anda menandatangani sambil berdiri atau terburu-buru, sudut goresan bisa berubah 15-30% sehingga terdeteksi berbeda dari KTP.",
                accentColor = Color(0xFF38BDF8)
            )

            // Tip 2: Unforgeable signatures
            TipItem(
                number = "2",
                title = "Cara Membuat Tanda Tangan Sulit Dipalsukan",
                description = "Gunakan variasi kecepatan goresan: kombinasi antara tarikan cepat dan lekukan presisi. Pemalsu biasanya menggambar lambat sehingga menghasilkan getaran mikro (tremor) yang mudah dideteksi aplikasi ini.",
                accentColor = GenuineGreen
            )

            // Tip 3: Kuitansi & Nota
            TipItem(
                number = "3",
                title = "Memeriksa Tanda Tangan Kuitansi & Nota",
                description = "Pada kuitansi pembayaran besar atau sewa kos, selalu foto tanda tangan penerima dengan pencahayaan terang dan tegak lurus (90 derajat) agar sistem dapat menganalisis proporsi piksel secara akurat.",
                accentColor = Color(0xFFA855F7)
            )

            // Tip 4: Brankas Pribadi
            TipItem(
                number = "4",
                title = "Simpan Contoh Tanda Tangan di Brankas Aplikasi",
                description = "Gunakan menu 'Spesimen' untuk menyimpan foto tanda tangan KTP dan buku rekening Anda sendiri. Anda bisa mencocokkannya kapan pun sebelum menandatangani berkas penting.",
                accentColor = Color(0xFFF59E0B)
            )
        }
    }
}

@Composable
fun TipItem(
    number: String,
    title: String,
    description: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MidnightCardBg)
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                fontWeight = FontWeight.Black,
                color = accentColor,
                fontSize = 13.sp
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = Color(0xFFCBD5E1),
                lineHeight = 16.sp
            )
        }
    }
}
