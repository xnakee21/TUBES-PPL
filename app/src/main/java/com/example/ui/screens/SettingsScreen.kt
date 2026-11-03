package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApiRequestType
import com.example.data.model.ApiSettings
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GenuineGreen
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.MidnightSurfaceVariant
import com.example.ui.viewmodel.SignatureViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    viewModel: SignatureViewModel,
    onBack: () -> Unit,
    onReplaySplash: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentSettings by viewModel.apiSettings.collectAsState()
    val apiTestStatus by viewModel.apiTestStatus.collectAsState()
    val isTestingApi by viewModel.isTestingApi.collectAsState()

    var endpointUrl by remember(currentSettings) { mutableStateOf(currentSettings.endpointUrl) }
    var authToken by remember(currentSettings) { mutableStateOf(currentSettings.authToken) }
    var requestType by remember(currentSettings) { mutableStateOf(currentSettings.requestType) }
    var thresholdSlider by remember(currentSettings) { mutableFloatStateOf(currentSettings.thresholdPercent.toFloat()) }
    var fallbackToLocal by remember(currentSettings) { mutableStateOf(currentSettings.fallbackToLocalEngine) }

    var showApiDoc by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Pengaturan Model ML & API",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Konfigurasi koneksi ke server Machine Learning Anda",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // Endpoint Configuration Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MidnightSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = CyanAccent)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "URL Endpoint API Model",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    OutlinedTextField(
                        value = endpointUrl,
                        onValueChange = { endpointUrl = it },
                        label = { Text("API Endpoint URL") },
                        placeholder = { Text("http://192.168.1.5:5000/predict") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_api_url"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = authToken,
                        onValueChange = { authToken = it },
                        label = { Text("Authorization Token (Opsional)") },
                        placeholder = { Text("Bearer token / API key jika dibutuhkan") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_api_token"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Text(
                        text = "Format Pengiriman Gambar:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { requestType = ApiRequestType.MULTIPART_FORM }
                        ) {
                            RadioButton(
                                selected = requestType == ApiRequestType.MULTIPART_FORM,
                                onClick = { requestType = ApiRequestType.MULTIPART_FORM },
                                colors = RadioButtonDefaults.colors(selectedColor = CyanAccent)
                            )
                            Text("Multipart (File)", fontSize = 12.sp, color = Color.White)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { requestType = ApiRequestType.JSON_BASE64 }
                        ) {
                            RadioButton(
                                selected = requestType == ApiRequestType.JSON_BASE64,
                                onClick = { requestType = ApiRequestType.JSON_BASE64 },
                                colors = RadioButtonDefaults.colors(selectedColor = CyanAccent)
                            )
                            Text("JSON (Base64)", fontSize = 12.sp, color = Color.White)
                        }
                    }

                    // Test Connection Button
                    OutlinedButton(
                        onClick = {
                            viewModel.updateApiSettings(
                                ApiSettings(
                                    endpointUrl = endpointUrl,
                                    authToken = authToken,
                                    requestType = requestType,
                                    thresholdPercent = thresholdSlider.roundToInt(),
                                    fallbackToLocalEngine = fallbackToLocal
                                )
                            )
                            viewModel.testApiConnection()
                        },
                        enabled = !isTestingApi,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_api_connection_btn")
                    ) {
                        if (isTestingApi) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = CyanAccent, strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Menguji Koneksi...")
                        } else {
                            Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Uji Koneksi (Ping API)")
                        }
                    }

                    if (apiTestStatus != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MidnightDark)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = apiTestStatus!!,
                                fontSize = 12.sp,
                                color = if (apiTestStatus!!.startsWith("SUKSES")) GenuineGreen else CyanAccent
                            )
                        }
                    }
                }
            }
        }

        // Decision Threshold & Fallback
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MidnightSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = CyanAccent)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Ambang Batas Keaslian (Threshold)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "${thresholdSlider.roundToInt()}%",
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            fontSize = 16.sp
                        )
                    }

                    Text(
                        text = "Skor di atas ambang ini dinyatakan ASLI. Di bawah ambang dinyatakan PALSU.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )

                    Slider(
                        value = thresholdSlider,
                        onValueChange = { thresholdSlider = it },
                        valueRange = 50f..95f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = CyanAccent,
                            activeTrackColor = CyanAccent
                        ),
                        modifier = Modifier.testTag("threshold_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fallback ke Engine Biometrik Lokal",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Otomatis gunakan analisis lokal jika server API sedang offline/gangguan",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Switch(
                            checked = fallbackToLocal,
                            onCheckedChange = { fallbackToLocal = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MidnightDark,
                                checkedTrackColor = CyanAccent
                            )
                        )
                    }
                }
            }
        }

        // Save Settings Button
        item {
            Button(
                onClick = {
                    viewModel.updateApiSettings(
                        ApiSettings(
                            endpointUrl = endpointUrl,
                            authToken = authToken,
                            requestType = requestType,
                            thresholdPercent = thresholdSlider.roundToInt(),
                            fallbackToLocalEngine = fallbackToLocal,
                            activeProfile = currentSettings.activeProfile
                        )
                    )
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = MidnightDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_settings_btn")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Simpan Pengaturan API", fontWeight = FontWeight.Bold)
            }
        }

        // Developer / API Documentation Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MidnightSurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showApiDoc = !showApiDoc },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = CyanAccent)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Dokumentasi & Spesifikasi API",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Icon(
                            imageVector = if (showApiDoc) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    AnimatedVisibility(visible = showApiDoc) {
                        Column(
                            modifier = Modifier.padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Aplikasi ini dapat mengirimkan berkas tanda tangan langsung ke API backend Anda (Python FastAPI, Flask, Django, Node, dll).",
                                fontSize = 12.sp,
                                color = Color(0xFFCBD5E1)
                            )

                            Text(
                                text = "Contoh Respon JSON yang Didukung:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = CyanAccent
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MidnightDark)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = """
{
  "status": "asli",
  "confidence": 96.4,
  "is_genuine": true,
  "similarity": 0.964
}
                                    """.trimIndent(),
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

        // Visual & Animation Experience Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.MidnightCardBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(Color(0x3300E5FF), Color(0x0D1E293B))))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyanAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Animasi & Efek Visual",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Pemindaian biometrik futuristik dan grafis cyber forensik",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onReplaySplash,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("replay_splash_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Putar Ulang Animasi Pembuka (Intro Scanner)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
