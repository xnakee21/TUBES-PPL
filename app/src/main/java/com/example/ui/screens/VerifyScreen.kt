package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReferenceSpecimen
import com.example.data.model.VerificationMode
import com.example.ui.components.SignatureCanvas
import com.example.ui.components.scannerCorners
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ForgeryRed
import com.example.ui.theme.GenuineGreen
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.MidnightSurfaceVariant
import com.example.ui.viewmodel.SignatureViewModel
import com.example.ui.viewmodel.VerificationState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyScreen(
    viewModel: SignatureViewModel,
    onVerificationCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val verificationMode by viewModel.verificationMode.collectAsState()
    val sampleBitmap by viewModel.sampleBitmap.collectAsState()
    val referenceBitmap by viewModel.referenceBitmap.collectAsState()
    val selectedReference by viewModel.selectedReference.collectAsState()
    val useExternalApi by viewModel.useExternalApi.collectAsState()
    val verificationState by viewModel.verificationState.collectAsState()
    val apiSettings by viewModel.apiSettings.collectAsState()
    val allSpecimens by viewModel.allSpecimens.collectAsState()

    var isDrawingForSample by remember { mutableStateOf(false) }
    var isDrawingForReference by remember { mutableStateOf(false) }
    var isCameraForSample by remember { mutableStateOf(false) }
    var isCameraForReference by remember { mutableStateOf(false) }
    var showSpecimenPicker by remember { mutableStateOf(false) }

    // Android Photo Picker for Sample
    val samplePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val bmp = loadBitmap(context, it)
            viewModel.setSampleBitmap(bmp)
        }
    }

    // Android Photo Picker for Reference
    val referencePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val bmp = loadBitmap(context, it)
            viewModel.setReferenceBitmap(bmp)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            val activeProfile by viewModel.inspectionProfile.collectAsState()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Verifikasi Tanda Tangan",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Bandingkan sampel dengan acuan resmi atau brankas",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeProfile == com.example.data.model.InspectionProfile.GENERAL) GenuineGreen.copy(alpha = 0.15f) else Color(0x3338BDF8))
                        .border(1.dp, if (activeProfile == com.example.data.model.InspectionProfile.GENERAL) GenuineGreen else Color(0xFF38BDF8), RoundedCornerShape(8.dp))
                        .clickable {
                            val next = if (activeProfile == com.example.data.model.InspectionProfile.GENERAL)
                                com.example.data.model.InspectionProfile.PROFESSIONAL
                            else
                                com.example.data.model.InspectionProfile.GENERAL
                            viewModel.setInspectionProfile(next)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = activeProfile.badgeLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeProfile == com.example.data.model.InspectionProfile.GENERAL) GenuineGreen else Color(0xFF38BDF8)
                    )
                }
            }
        }

        // Mode Switcher Tabs
        item {
            TabRow(
                selectedTabIndex = if (verificationMode == VerificationMode.COMPARISON) 0 else 1,
                containerColor = MidnightSurface,
                contentColor = CyanAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(
                            tabPositions[if (verificationMode == VerificationMode.COMPARISON) 0 else 1]
                        ),
                        color = CyanAccent
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = verificationMode == VerificationMode.COMPARISON,
                    onClick = { viewModel.setVerificationMode(VerificationMode.COMPARISON) },
                    text = {
                        Text(
                            text = "Komparasi 1:1",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_mode_comparison")
                )
                Tab(
                    selected = verificationMode == VerificationMode.STANDALONE,
                    onClick = { viewModel.setVerificationMode(VerificationMode.STANDALONE) },
                    text = {
                        Text(
                            text = "Model AI Mandiri",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_mode_standalone")
                )
            }
        }

        // Quick Preset Testing Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.loadPresetDemoSignatures(asli = true) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("load_preset_asli")
                ) {
                    Text("Muat Contoh Asli", fontSize = 12.sp, color = GenuineGreen)
                }

                OutlinedButton(
                    onClick = { viewModel.loadPresetDemoSignatures(asli = false) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("load_preset_palsu")
                ) {
                    Text("Muat Contoh Palsu", fontSize = 12.sp, color = ForgeryRed)
                }
            }
        }

        // Sample Signature Input Container
        item {
            SignatureInputCard(
                title = "1. Tanda Tangan Sampel (Yang Ingin Diuji)",
                bitmap = sampleBitmap,
                onDrawClicked = { isDrawingForSample = true },
                onCameraClicked = { isCameraForSample = true },
                onUploadClicked = {
                    samplePhotoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onClearClicked = { viewModel.setSampleBitmap(null) },
                testTagPrefix = "sample_sig"
            )
        }

        // Reference Signature Input Container (Only shown in 1:1 Comparison mode)
        if (verificationMode == VerificationMode.COMPARISON) {
            item {
                SignatureInputCard(
                    title = "2. Tanda Tangan Referensi (Spesimen Asli)",
                    subtitle = selectedReference?.let { "Spesimen: ${it.name} (${it.roleOrTitle})" }
                        ?: "Gunakan spesimen KTP / arsip resmi untuk membandingkan goresan",
                    bitmap = referenceBitmap,
                    onDrawClicked = { isDrawingForReference = true },
                    onCameraClicked = { isCameraForReference = true },
                    onUploadClicked = {
                        referencePhotoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onPickFromBank = { showSpecimenPicker = true },
                    onClearClicked = { viewModel.setReferenceBitmap(null) },
                    testTagPrefix = "reference_sig"
                )
            }
        }

        // Error message banner if any
        if (verificationState is VerificationState.Failed) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ForgeryRed.copy(alpha = 0.15f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ForgeryRed))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = (verificationState as VerificationState.Failed).error,
                            color = ForgeryRed,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Primary Verification Submit Button
        item {
            val isProcessing = verificationState is VerificationState.Processing
            val isReady = !isProcessing && sampleBitmap != null

            Button(
                onClick = {
                    val defaultTitle = if (verificationMode == VerificationMode.COMPARISON) {
                        selectedReference?.let { "Verifikasi vs ${it.name}" } ?: "Verifikasi Tanda Tangan (1:1)"
                    } else {
                        "Verifikasi Mandiri AI"
                    }
                    val defaultSigner = selectedReference?.name ?: "Subjek Penandatangan"
                    viewModel.startVerification(defaultTitle, defaultSigner)
                },
                enabled = isReady,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color(0xFF1E293B)
                ),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .then(
                        if (isReady) Modifier.background(com.example.ui.theme.PrimaryCyberGradient, RoundedCornerShape(16.dp))
                        else Modifier
                    )
                    .testTag("submit_verification_button")
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MidnightDark,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Menganalisis Biometrik...",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = MidnightDark
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (isReady) MidnightDark else Color(0xFF64748B),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Jalankan Deteksi Keaslian",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = if (isReady) MidnightDark else Color(0xFF64748B),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }

    // Modal Canvas for Sample
    if (isDrawingForSample) {
        ModalBottomSheet(
            onDismissRequest = { isDrawingForSample = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MidnightDark
        ) {
            SignatureCanvas(
                onSignatureConfirmed = { bitmap ->
                    viewModel.setSampleBitmap(bitmap)
                    isDrawingForSample = false
                },
                onCancel = { isDrawingForSample = false }
            )
        }
    }

    // Modal Canvas for Reference
    if (isDrawingForReference) {
        ModalBottomSheet(
            onDismissRequest = { isDrawingForReference = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MidnightDark
        ) {
            SignatureCanvas(
                onSignatureConfirmed = { bitmap ->
                    viewModel.setReferenceBitmap(bitmap)
                    isDrawingForReference = false
                },
                onCancel = { isDrawingForReference = false }
            )
        }
    }

    // Camera Capture for Sample
    if (isCameraForSample) {
        ModalBottomSheet(
            onDismissRequest = { isCameraForSample = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.Black
        ) {
            CameraCaptureScreen(
                title = "Pindai Tanda Tangan Sampel",
                onPhotoCaptured = { bitmap ->
                    viewModel.setSampleBitmap(bitmap)
                    isCameraForSample = false
                },
                onCancel = { isCameraForSample = false }
            )
        }
    }

    // Camera Capture for Reference
    if (isCameraForReference) {
        ModalBottomSheet(
            onDismissRequest = { isCameraForReference = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.Black
        ) {
            CameraCaptureScreen(
                title = "Pindai Spesimen Referensi",
                onPhotoCaptured = { bitmap ->
                    viewModel.setReferenceBitmap(bitmap)
                    isCameraForReference = false
                },
                onCancel = { isCameraForReference = false }
            )
        }
    }

    // Modal Sheet for choosing from Specimen Bank
    if (showSpecimenPicker) {
        ModalBottomSheet(
            onDismissRequest = { showSpecimenPicker = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MidnightDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Pilih Spesimen Terdaftar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (allSpecimens.isEmpty()) {
                    Text(
                        text = "Belum ada spesimen resmi. Tambahkan di menu Bank Spesimen.",
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(allSpecimens) { specimen ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectReferenceSpecimen(specimen)
                                        showSpecimenPicker = false
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MidnightSurfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = specimen.name,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = specimen.roleOrTitle,
                                            color = Color(0xFF94A3B8),
                                            fontSize = 12.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Pilih",
                                        tint = CyanAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Auto-navigate to result when completed
    if (verificationState is VerificationState.Completed) {
        onVerificationCompleted()
    }
}

@Composable
fun SignatureInputCard(
    title: String,
    subtitle: String? = null,
    bitmap: Bitmap?,
    onDrawClicked: () -> Unit,
    onCameraClicked: () -> Unit,
    onUploadClicked: () -> Unit,
    onPickFromBank: (() -> Unit)? = null,
    onClearClicked: () -> Unit,
    testTagPrefix: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            }

            // Preview Box with Scanner Reticles
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF080D1A))
                    .scannerCorners(if (bitmap != null) CyanAccent else Color(0xFF334155), 16.dp, 2.dp)
                    .border(
                        1.dp,
                        if (bitmap != null) Color(0x3300E5FF) else Color(0xFF1E293B),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Tanda tangan terpilih",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                    )

                    // Subtle animated biometric laser scanline
                    com.example.ui.components.BiometricScanline()

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xCC0F172A))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "DIMUAT • SIAP VERIFIKASI",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            letterSpacing = 0.5.sp
                        )
                    }

                    IconButton(
                        onClick = onClearClicked,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(32.dp)
                            .background(Color(0xCC000000), CircleShape)
                            .testTag("${testTagPrefix}_clear")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hapus",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Create,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Belum ada tanda tangan dimuat",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Pilih Canvas, Kamera, Galeri, atau Spesimen",
                            color = Color(0xFF475569),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onDrawClicked,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("${testTagPrefix}_draw_btn")
                ) {
                    Icon(Icons.Default.Create, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Canvas", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onCameraClicked,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("${testTagPrefix}_camera_btn")
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Kamera", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onUploadClicked,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("${testTagPrefix}_upload_btn")
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Galeri", fontSize = 11.sp)
                }

                if (onPickFromBank != null) {
                    OutlinedButton(
                        onClick = onPickFromBank,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("${testTagPrefix}_bank_btn")
                    ) {
                        Icon(Icons.Default.FolderShared, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Spesimen", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

private fun loadBitmap(context: android.content.Context, uri: Uri): Bitmap? {
    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }.getOrNull()
}
