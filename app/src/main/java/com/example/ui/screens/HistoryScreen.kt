package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SignatureBiometrics
import com.example.data.model.VerificationRecord
import com.example.data.model.VerificationVerdict
import com.example.ui.components.BiometricsCard
import com.example.ui.components.VerdictBadge
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ForgeryRed
import com.example.ui.theme.GenuineGreen
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.MidnightSurfaceVariant
import com.example.ui.viewmodel.SignatureViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: SignatureViewModel,
    modifier: Modifier = Modifier
) {
    val records by viewModel.allRecords.collectAsState()
    val filter by viewModel.historyFilter.collectAsState()
    val searchQuery by viewModel.historySearchQuery.collectAsState()

    var showClearDialog by remember { mutableStateOf(false) }
    var selectedRecordForDetail by remember { mutableStateOf<VerificationRecord?>(null) }

    val filteredRecords = records.filter { record ->
        val matchesFilter = when (filter) {
            null -> true
            else -> record.verdict == filter
        }
        val matchesQuery = if (searchQuery.isBlank()) true else {
            record.documentTitle.contains(searchQuery, ignoreCase = true) ||
            record.signerName.contains(searchQuery, ignoreCase = true)
        }
        matchesFilter && matchesQuery
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
                Column {
                    Text(
                        text = "Arsip Riwayat Verifikasi",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${filteredRecords.size} catatan ditemukan",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }

                if (records.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Hapus Semua Riwayat",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setHistorySearchQuery(it) },
                placeholder = { Text("Cari riwayat pemeriksaan...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = CyanAccent)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setHistorySearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Hapus pencarian", tint = Color(0xFF94A3B8))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_history_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Filter Chips Row
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filter == null,
                    onClick = { viewModel.setHistoryFilter(null) },
                    label = { Text("Semua") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanAccent,
                        selectedLabelColor = MidnightDark
                    ),
                    modifier = Modifier.testTag("filter_all")
                )

                FilterChip(
                    selected = filter == VerificationVerdict.ASLI,
                    onClick = { viewModel.setHistoryFilter(VerificationVerdict.ASLI) },
                    label = { Text("Asli") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GenuineGreen,
                        selectedLabelColor = MidnightDark
                    ),
                    modifier = Modifier.testTag("filter_asli")
                )

                FilterChip(
                    selected = filter == VerificationVerdict.PALSU,
                    onClick = { viewModel.setHistoryFilter(VerificationVerdict.PALSU) },
                    label = { Text("Palsu") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ForgeryRed,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_palsu")
                )
            }
        }

        if (filteredRecords.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Tidak ada rekaman riwayat yang sesuai",
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(filteredRecords) { record ->
                HistoryDetailCard(
                    record = record,
                    onClick = { selectedRecordForDetail = record },
                    onDelete = { viewModel.deleteRecord(record) }
                )
            }
        }
    }

    // Detail Dialog for past record
    selectedRecordForDetail?.let { record ->
        AlertDialog(
            onDismissRequest = { selectedRecordForDetail = null },
            title = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = record.documentTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = record.signerName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VerdictBadge(verdict = record.verdict)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MidnightSurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${record.apiSource} • ${record.latencyMs} ms",
                                color = CyanAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(Modifier.height(2.dp))
                    BiometricsCard(
                        biometrics = SignatureBiometrics(
                            strokeContinuity = record.strokeContinuity,
                            aspectProportionMatch = record.aspectProportionMatch,
                            pixelDensityConsistency = record.pixelDensityConsistency,
                            baselineSlope = record.baselineSlope,
                            structuralSimilarity = record.structuralSimilarity,
                            smoothnessScore = record.smoothnessScore,
                            overallScore = record.confidence
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedRecordForDetail = null }) {
                    Text("Tutup", color = CyanAccent, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = MidnightSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Clear confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Hapus Semua Riwayat?", color = Color.White) },
            text = { Text("Semua data riwayat verifikasi akan dihapus secara permanen dari perangkat.", color = Color(0xFF94A3B8)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("Hapus", color = ForgeryRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Batal", color = Color(0xFF94A3B8))
                }
            },
            containerColor = MidnightSurface
        )
    }
}

@Composable
fun HistoryDetailCard(
    record: VerificationRecord,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(record.timestamp) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date(record.timestamp))
    }

    val isAsli = record.verdict == VerificationVerdict.ASLI
    val statusColor = if (isAsli) GenuineGreen else ForgeryRed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("history_record_${record.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.MidnightCardBg),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                listOf(statusColor.copy(alpha = 0.35f), Color(0x0D1E293B))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Avatar + Title & Date + Delete action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Biometric Monogram
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.5.dp, statusColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = record.signerName.trim().take(1).uppercase().ifBlank { "S" }
                    Text(
                        text = initial,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = statusColor
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.documentTitle,
                        style = MaterialTheme.typography.titleMedium,
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

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Riwayat",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Divider line subtle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF1E293B))
            )

            // Bottom Tier: Metrics Badge & Engine source + Verdict Badge
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
                    // Similarity pill
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

                    // Engine Source pill
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
