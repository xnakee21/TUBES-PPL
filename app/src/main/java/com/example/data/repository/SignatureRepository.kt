package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.net.Uri
import com.example.data.engine.SignatureBiometricEngine
import com.example.data.local.SignatureDatabase
import com.example.data.model.ApiRequestType
import com.example.data.model.ApiSettings
import com.example.data.model.ReferenceSpecimen
import com.example.data.model.SignatureBiometrics
import com.example.data.model.VerificationMode
import com.example.data.model.VerificationRecord
import com.example.data.model.VerificationVerdict
import com.example.data.network.ApiResponse
import com.example.data.network.SignatureApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class VerificationProcessResult(
    val verdict: VerificationVerdict,
    val confidence: Float,
    val biometrics: SignatureBiometrics,
    val diffBitmap: Bitmap?,
    val sourceLabel: String,
    val latencyMs: Long,
    val rawResponse: String? = null
)

class SignatureRepository(
    private val context: Context,
    private val database: SignatureDatabase = SignatureDatabase.getInstance(context),
    private val apiClient: SignatureApiClient = SignatureApiClient(),
    private val biometricEngine: SignatureBiometricEngine = SignatureBiometricEngine()
) {
    private val prefs = context.getSharedPreferences("signature_prefs", Context.MODE_PRIVATE)

    fun getAllRecords(): Flow<List<VerificationRecord>> = database.verificationDao().getAllRecords()

    fun getAllSpecimens(): Flow<List<ReferenceSpecimen>> = database.referenceDao().getAllSpecimens()

    suspend fun saveRecord(record: VerificationRecord): Long = database.verificationDao().insertRecord(record)

    suspend fun deleteRecord(record: VerificationRecord) = database.verificationDao().deleteRecord(record)

    suspend fun clearAllRecords() = database.verificationDao().clearAll()

    suspend fun saveSpecimen(specimen: ReferenceSpecimen): Long = database.referenceDao().insertSpecimen(specimen)

    suspend fun deleteSpecimen(specimen: ReferenceSpecimen) = database.referenceDao().deleteSpecimen(specimen)

    fun getApiSettings(): ApiSettings {
        val url = prefs.getString("endpoint_url", "http://10.0.2.2:5000/predict") ?: "http://10.0.2.2:5000/predict"
        val token = prefs.getString("auth_token", "") ?: ""
        val reqTypeStr = prefs.getString("request_type", ApiRequestType.MULTIPART_FORM.name)
        val reqType = runCatching { ApiRequestType.valueOf(reqTypeStr!!) }.getOrDefault(ApiRequestType.MULTIPART_FORM)
        val profileStr = prefs.getString("active_profile", com.example.data.model.InspectionProfile.GENERAL.name)
        val profile = runCatching { com.example.data.model.InspectionProfile.valueOf(profileStr!!) }.getOrDefault(com.example.data.model.InspectionProfile.GENERAL)
        val threshold = prefs.getInt("threshold_percent", profile.defaultThreshold)
        val fallback = prefs.getBoolean("fallback_local", true)

        return ApiSettings(
            endpointUrl = url,
            authToken = token,
            requestType = reqType,
            thresholdPercent = threshold,
            fallbackToLocalEngine = fallback,
            activeProfile = profile
        )
    }

    fun saveApiSettings(settings: ApiSettings) {
        prefs.edit()
            .putString("endpoint_url", settings.endpointUrl)
            .putString("auth_token", settings.authToken)
            .putString("request_type", settings.requestType.name)
            .putString("active_profile", settings.activeProfile.name)
            .putInt("threshold_percent", settings.thresholdPercent)
            .putBoolean("fallback_local", settings.fallbackToLocalEngine)
            .apply()
    }

    suspend fun testApiConnection(): ApiResponse<String> {
        val settings = getApiSettings()
        return apiClient.testConnection(settings)
    }

    suspend fun verifySignature(
        sampleBitmap: Bitmap,
        referenceBitmap: Bitmap?,
        mode: VerificationMode,
        useExternalApi: Boolean
    ): VerificationProcessResult = withContext(Dispatchers.Default) {
        val settings = getApiSettings()
        val start = System.currentTimeMillis()

        if (useExternalApi) {
            when (val apiRes = apiClient.verifySignature(sampleBitmap, referenceBitmap, settings)) {
                is ApiResponse.Success -> {
                    val pred = apiRes.data
                    // If reference bitmap is present, generate comparison visual diff using biometric engine
                    val diff = if (referenceBitmap != null) {
                        biometricEngine.compareSignatures(sampleBitmap, referenceBitmap, settings.thresholdPercent).third
                    } else null

                    return@withContext VerificationProcessResult(
                        verdict = pred.verdict,
                        confidence = pred.confidence,
                        biometrics = pred.biometrics,
                        diffBitmap = diff,
                        sourceLabel = "API Model ML Eksternal",
                        latencyMs = apiRes.latencyMs,
                        rawResponse = pred.rawResponse
                    )
                }
                is ApiResponse.Error -> {
                    if (!settings.fallbackToLocalEngine) {
                        return@withContext VerificationProcessResult(
                            verdict = VerificationVerdict.MENCURIGAKAN,
                            confidence = 0f,
                            biometrics = SignatureBiometrics(),
                            diffBitmap = null,
                            sourceLabel = "Error API: ${apiRes.message}",
                            latencyMs = apiRes.latencyMs,
                            rawResponse = apiRes.message
                        )
                    }
                    // Fallback to local forensic engine
                }
            }
        }

        // On-Device Computer Vision & Biometric Pipeline
        if (mode == VerificationMode.COMPARISON && referenceBitmap != null) {
            val (verdict, biometrics, diff) = biometricEngine.compareSignatures(
                sampleBitmap,
                referenceBitmap,
                settings.thresholdPercent
            )
            val elapsed = System.currentTimeMillis() - start
            VerificationProcessResult(
                verdict = verdict,
                confidence = biometrics.overallScore,
                biometrics = biometrics,
                diffBitmap = diff,
                sourceLabel = "Engine Biometrik Lokal",
                latencyMs = elapsed
            )
        } else {
            val (verdict, biometrics) = biometricEngine.analyzeSingle(sampleBitmap)
            val elapsed = System.currentTimeMillis() - start
            VerificationProcessResult(
                verdict = verdict,
                confidence = biometrics.overallScore,
                biometrics = biometrics,
                diffBitmap = null,
                sourceLabel = "Engine Biometrik Lokal",
                latencyMs = elapsed
            )
        }
    }

    suspend fun saveBitmapToInternalStorage(bitmap: Bitmap, prefix: String = "sig"): String = withContext(Dispatchers.IO) {
        val filename = "${prefix}_${UUID.randomUUID()}.png"
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        Uri.fromFile(file).toString()
    }

    suspend fun loadBitmapFromUri(uriString: String): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "file") {
                BitmapFactory.decodeFile(uri.path)
            } else {
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it)
                }
            }
        }.getOrNull()
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val currentCount = prefs.getBoolean("has_seeded_initial_v2", false)
        if (!currentCount) {
            // 1. Tanda Tangan KTP Pribadi (Umum)
            val ktpBitmap = createSyntheticSignatureBitmap(isGenuine = true)
            val ktpUri = saveBitmapToInternalStorage(ktpBitmap, "specimen_ktp")
            database.referenceDao().insertSpecimen(
                ReferenceSpecimen(
                    name = "Tanda Tangan KTP Saya",
                    roleOrTitle = "Identitas Pribadi (KTP / SIM)",
                    category = "KTP & Identitas",
                    imageUri = ktpUri,
                    notes = "Acuan tanda tangan resmi kartu identitas KTP & SIM pribadi"
                )
            )

            // 2. Tanda Tangan Buku Rekening Bank (Umum)
            val bankBitmap = createSyntheticSignatureBitmap(isGenuine = true)
            val bankUri = saveBitmapToInternalStorage(bankBitmap, "specimen_bank")
            database.referenceDao().insertSpecimen(
                ReferenceSpecimen(
                    name = "Tanda Tangan Rekening Bank",
                    roleOrTitle = "Spesimen Buku Tabungan Bank",
                    category = "Perbankan",
                    imageUri = bankUri,
                    notes = "Spesimen tanda tangan untuk transaksi teller dan pembukaan tabungan"
                )
            )

            // 3. Tanda Tangan Kontrak Bisnis (Formal)
            val formalBitmap = createSyntheticSignatureBitmap(isGenuine = true)
            val formalUri = saveBitmapToInternalStorage(formalBitmap, "specimen_formal")
            database.referenceDao().insertSpecimen(
                ReferenceSpecimen(
                    name = "Bambang Sudirman, S.H.",
                    roleOrTitle = "Direktur Utama PT Mega Sentosa",
                    category = "Formal & Bisnis",
                    imageUri = formalUri,
                    notes = "Spesimen resmi tanda tangan perjanjian kontrak kerja sama"
                )
            )

            // Seed sample past verification records (General & Formal)
            val verifiedSample = createSyntheticSignatureBitmap(isGenuine = true)
            val sampleUri = saveBitmapToInternalStorage(verifiedSample, "verified_sample")
            database.verificationDao().insertRecord(
                VerificationRecord(
                    documentTitle = "Kuitansi Pembayaran Sewa Kos",
                    signerName = "Ibu Hj. Maryam (Pemilik Kos)",
                    verdict = VerificationVerdict.ASLI,
                    confidence = 92.8f,
                    mode = VerificationMode.COMPARISON,
                    sampleImageUri = sampleUri,
                    referenceImageUri = ktpUri,
                    apiSource = "Engine Biometrik Lokal",
                    latencyMs = 180,
                    notes = "Tanda tangan cocok dengan acuan kuitansi bulan lalu",
                    strokeContinuity = 91.4f,
                    aspectProportionMatch = 94.0f,
                    pixelDensityConsistency = 92.5f,
                    baselineSlope = -0.8f,
                    structuralSimilarity = 93.1f,
                    smoothnessScore = 90.2f
                )
            )

            prefs.edit().putBoolean("has_seeded_initial_v2", true).apply()
        }
    }

    fun createSyntheticSignatureBitmap(isGenuine: Boolean): Bitmap {
        val width = 400
        val height = 240
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Clean white/transparent background
        canvas.drawColor(android.graphics.Color.TRANSPARENT)

        val paint = Paint().apply {
            color = android.graphics.Color.rgb(20, 30, 60)
            strokeWidth = if (isGenuine) 5.5f else 4.0f
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        val path = Path()
        if (isGenuine) {
            // Smooth, fluid calligraphic loops
            path.moveTo(60f, 150f)
            path.cubicTo(90f, 60f, 130f, 70f, 160f, 140f)
            path.cubicTo(180f, 190f, 210f, 80f, 250f, 130f)
            path.cubicTo(270f, 150f, 310f, 110f, 340f, 140f)
            // Signature underline swoop
            path.moveTo(80f, 175f)
            path.quadTo(200f, 195f, 350f, 160f)
        } else {
            // Trembling hesitation, angular jagged lines typical of forgery
            path.moveTo(60f, 150f)
            path.lineTo(75f, 120f)
            path.lineTo(95f, 80f)
            path.lineTo(110f, 130f)
            path.lineTo(135f, 110f)
            path.lineTo(170f, 170f)
            path.lineTo(195f, 100f)
            path.lineTo(235f, 140f)
            path.lineTo(280f, 135f)
            path.lineTo(330f, 160f)
            // Ragged disjoint underline
            path.moveTo(80f, 185f)
            path.lineTo(180f, 180f)
            path.lineTo(260f, 195f)
            path.lineTo(340f, 175f)
        }

        canvas.drawPath(path, paint)
        return bitmap
    }
}
