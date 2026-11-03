package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ReferenceSpecimen
import com.example.ui.components.SignatureCanvas
import com.example.ui.components.scannerCorners
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.MidnightSurfaceVariant
import com.example.ui.viewmodel.SignatureViewModel

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecimenBankScreen(
    viewModel: SignatureViewModel,
    onUseSpecimenForVerify: (ReferenceSpecimen) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val specimens by viewModel.allSpecimens.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var isDrawingCanvas by remember { mutableStateOf(false) }
    var isCameraCapture by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newRole by remember { mutableStateOf("") }
    var newNotes by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("KTP & Identitas") }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf("Semua") }

    val categories = listOf("Semua", "KTP & Identitas", "Perbankan", "Paraf Pribadi", "Formal & Bisnis")
    val filteredSpecimens = remember(specimens, selectedCategoryFilter) {
        if (selectedCategoryFilter == "Semua") specimens
        else specimens.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val bmp = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ -> decoder.isMutableRequired = true }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
            }.getOrNull()
            currentBitmap = bmp
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Brankas Tanda Tangan Saya",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Simpan acuan tanda tangan KTP, bank, & paraf pribadi Anda",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }

                Button(
                    onClick = {
                        newName = ""
                        newRole = ""
                        newNotes = ""
                        newCategory = "KTP & Identitas"
                        currentBitmap = null
                        showAddDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = MidnightDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_specimen_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Tambah", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
                            selectedLabelColor = CyanAccent,
                            containerColor = MidnightSurface,
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color(0xFF334155),
                            selectedBorderColor = CyanAccent
                        )
                    )
                }
            }
        }

        if (filteredSpecimens.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderShared,
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = if (selectedCategoryFilter == "Semua")
                                "Belum ada tanda tangan tersimpan di brankas"
                            else
                                "Belum ada tanda tangan pada kategori $selectedCategoryFilter",
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(filteredSpecimens) { specimen ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("specimen_item_${specimen.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.MidnightCardBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0x3300E5FF), Color(0x0D1E293B))))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = specimen.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF0F172A))
                                            .border(1.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = specimen.category,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanAccent
                                        )
                                    }
                                }
                                Text(
                                    text = specimen.roleOrTitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (specimen.notes.isNotBlank()) {
                                    Text(
                                        text = specimen.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            IconButton(onClick = { viewModel.deleteSpecimen(specimen) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Signature Image Preview with scanner reticles
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF080D1A))
                                .scannerCorners(CyanAccent, 14.dp, 1.5.dp)
                                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = specimen.imageUri,
                                contentDescription = "Spesimen ${specimen.name}",
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = { onUseSpecimenForVerify(specimen) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanAccent.copy(alpha = 0.12f),
                                contentColor = CyanAccent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Gunakan Sebagai Referensi Verifikasi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet for Adding New Specimen
    if (showAddDialog) {
        ModalBottomSheet(
            onDismissRequest = { showAddDialog = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MidnightDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Tambah Tanda Tangan ke Brankas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Pilih Kategori:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                )

                val dialogCategories = listOf("KTP & Identitas", "Perbankan", "Paraf Pribadi", "Formal & Bisnis", "Lainnya")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(dialogCategories) { cat ->
                        val isSel = newCategory == cat
                        FilterChip(
                            selected = isSel,
                            onClick = { newCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent.copy(alpha = 0.25f),
                                selectedLabelColor = CyanAccent,
                                containerColor = MidnightSurface,
                                labelColor = Color(0xFF94A3B8)
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Label / Nama (Contoh: Tanda Tangan KTP Saya)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = newRole,
                    onValueChange = { newRole = it },
                    label = { Text("Keterangan Tambahan (Contoh: KTP 2024 / Bank BCA)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                // Preview or placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, if (currentBitmap != null) CyanAccent else Color(0xFF334155), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentBitmap != null) {
                        Image(
                            bitmap = currentBitmap!!.asImageBitmap(),
                            contentDescription = "Spesimen Preview",
                            modifier = Modifier.padding(10.dp)
                        )
                    } else {
                        Text("Belum ada gambar tanda tangan spesimen", color = Color(0xFF64748B), fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { isDrawingCanvas = true },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Create, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Canvas", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { isCameraCapture = true },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Kamera", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Galeri", fontSize = 11.sp)
                    }
                }

                Spacer(Modifier.height(6.dp))

                Button(
                    onClick = {
                        if (newName.isNotBlank() && currentBitmap != null) {
                            viewModel.addReferenceSpecimen(newName, newRole, currentBitmap!!, newNotes, newCategory)
                            showAddDialog = false
                        }
                    },
                    enabled = newName.isNotBlank() && currentBitmap != null,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = MidnightDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_specimen_btn")
                ) {
                    Text("Simpan ke Brankas Tanda Tangan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (isDrawingCanvas) {
        ModalBottomSheet(
            onDismissRequest = { isDrawingCanvas = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MidnightDark
        ) {
            SignatureCanvas(
                onSignatureConfirmed = { bitmap ->
                    currentBitmap = bitmap
                    isDrawingCanvas = false
                },
                onCancel = { isDrawingCanvas = false }
            )
        }
    }

    if (isCameraCapture) {
        ModalBottomSheet(
            onDismissRequest = { isCameraCapture = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.Black
        ) {
            CameraCaptureScreen(
                title = "Pindai Spesimen Tanda Tangan",
                onPhotoCaptured = { bitmap ->
                    currentBitmap = bitmap
                    isCameraCapture = false
                },
                onCancel = { isCameraCapture = false }
            )
        }
    }
}
