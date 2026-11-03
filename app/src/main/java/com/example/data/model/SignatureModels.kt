package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class VerificationVerdict(val labelIndo: String, val labelEnglish: String) {
    ASLI("Asli (Otentik)", "Genuine"),
    PALSU("Palsu (Tiruan)", "Forged"),
    MENCURIGAKAN("Perlu Analisis Ulang", "Inconclusive")
}

enum class VerificationMode(val displayName: String) {
    COMPARISON("Verifikasi Komparasi (1:1)"),
    STANDALONE("Klasifikasi Mandiri (Model AI)")
}

enum class InspectionProfile(
    val title: String,
    val subtitle: String,
    val defaultThreshold: Int,
    val badgeLabel: String
) {
    GENERAL(
        "Mode Umum & Harian",
        "Kuitansi, nota, surat izin, paket, & cek tanda tangan KTP pribadi",
        68,
        "MODE UMUM"
    ),
    PROFESSIONAL(
        "Mode Formal & Bisnis",
        "Kontrak kerja, perbankan, akta legal, & audit dokumen resmi",
        78,
        "MODE FORMAL"
    )
}

enum class GeneralUseCase(
    val title: String,
    val subtitle: String,
    val defaultDocTitle: String
) {
    RECEIPT("Kuitansi & Belanja", "Cek bukti bayar & transfer", "Kuitansi Pembayaran"),
    ID_CARD("Cek Tanda Tangan KTP", "Uji konsistensi sebelum ke bank", "Cek Tanda Tangan KTP"),
    CONTRACT("Surat Kontrak & Sewa", "Perjanjian sewa & pekerjaan", "Surat Perjanjian"),
    PACKAGE("Tanda Terima Paket", "Bukti serah terima ekspedisi", "Tanda Terima Barang")
}

data class SignatureBiometrics(
    val strokeContinuity: Float = 0f,       // 0 - 100%
    val aspectProportionMatch: Float = 0f,   // 0 - 100%
    val pixelDensityConsistency: Float = 0f,// 0 - 100%
    val baselineSlope: Float = 0f,           // Kemiringan sudut goresan
    val structuralSimilarity: Float = 0f,    // SSIM %
    val smoothnessScore: Float = 0f,         // Tingkat kelenturan goresan
    val overallScore: Float = 0f             // Skor akhir gabungan %
)

@Entity(tableName = "verification_records")
data class VerificationRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val documentTitle: String,
    val signerName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val verdict: VerificationVerdict,
    val confidence: Float,
    val mode: VerificationMode,
    val sampleImageUri: String,
    val referenceImageUri: String? = null,
    val apiSource: String, // "API Eksternal (ML Model)" or "On-Device Biometric Engine"
    val latencyMs: Long = 0,
    val notes: String = "",
    // Metrics serialized as individual floats for clean room persistence
    val strokeContinuity: Float = 0f,
    val aspectProportionMatch: Float = 0f,
    val pixelDensityConsistency: Float = 0f,
    val baselineSlope: Float = 0f,
    val structuralSimilarity: Float = 0f,
    val smoothnessScore: Float = 0f
)

@Entity(tableName = "reference_specimens")
data class ReferenceSpecimen(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val roleOrTitle: String,
    val registeredDate: Long = System.currentTimeMillis(),
    val imageUri: String,
    val notes: String = "",
    val category: String = "Umum"
)

data class ApiSettings(
    val endpointUrl: String = "http://10.0.2.2:5000/predict",
    val authToken: String = "",
    val requestType: ApiRequestType = ApiRequestType.MULTIPART_FORM,
    val thresholdPercent: Int = 68,
    val fallbackToLocalEngine: Boolean = true,
    val activeProfile: InspectionProfile = InspectionProfile.GENERAL
)

enum class ApiRequestType(val label: String) {
    MULTIPART_FORM("Multipart Form Data (file)"),
    JSON_BASE64("JSON Payload (base64)")
}
