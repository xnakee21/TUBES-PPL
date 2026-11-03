package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ApiSettings
import com.example.data.model.ReferenceSpecimen
import com.example.data.model.VerificationMode
import com.example.data.model.VerificationRecord
import com.example.data.model.VerificationVerdict
import com.example.data.network.ApiResponse
import com.example.data.repository.SignatureRepository
import com.example.data.repository.VerificationProcessResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class VerificationState {
    object Idle : VerificationState()
    data class Processing(val progressMessage: String) : VerificationState()
    data class Completed(val result: VerificationProcessResult) : VerificationState()
    data class Failed(val error: String) : VerificationState()
}

class SignatureViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SignatureRepository(application)

    val allRecords: StateFlow<List<VerificationRecord>> = repository.getAllRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSpecimens: StateFlow<List<ReferenceSpecimen>> = repository.getAllSpecimens()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Verification Workflow State
    private val _verificationMode = MutableStateFlow(VerificationMode.COMPARISON)
    val verificationMode: StateFlow<VerificationMode> = _verificationMode.asStateFlow()

    private val _sampleBitmap = MutableStateFlow<Bitmap?>(null)
    val sampleBitmap: StateFlow<Bitmap?> = _sampleBitmap.asStateFlow()

    private val _referenceBitmap = MutableStateFlow<Bitmap?>(null)
    val referenceBitmap: StateFlow<Bitmap?> = _referenceBitmap.asStateFlow()

    private val _selectedReference = MutableStateFlow<ReferenceSpecimen?>(null)
    val selectedReference: StateFlow<ReferenceSpecimen?> = _selectedReference.asStateFlow()

    private val _useExternalApi = MutableStateFlow(true)
    val useExternalApi: StateFlow<Boolean> = _useExternalApi.asStateFlow()

    private val _verificationState = MutableStateFlow<VerificationState>(VerificationState.Idle)
    val verificationState: StateFlow<VerificationState> = _verificationState.asStateFlow()

    private val _apiSettings = MutableStateFlow(repository.getApiSettings())
    val apiSettings: StateFlow<ApiSettings> = _apiSettings.asStateFlow()

    private val _inspectionProfile = MutableStateFlow(_apiSettings.value.activeProfile)
    val inspectionProfile: StateFlow<com.example.data.model.InspectionProfile> = _inspectionProfile.asStateFlow()

    private val _apiTestStatus = MutableStateFlow<String?>(null)
    val apiTestStatus: StateFlow<String?> = _apiTestStatus.asStateFlow()

    private val _isTestingApi = MutableStateFlow(false)
    val isTestingApi: StateFlow<Boolean> = _isTestingApi.asStateFlow()

    // History Filters
    private val _historyFilter = MutableStateFlow<VerificationVerdict?>(null)
    val historyFilter: StateFlow<VerificationVerdict?> = _historyFilter.asStateFlow()

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun setInspectionProfile(profile: com.example.data.model.InspectionProfile) {
        _inspectionProfile.value = profile
        val updated = _apiSettings.value.copy(
            activeProfile = profile,
            thresholdPercent = profile.defaultThreshold
        )
        _apiSettings.value = updated
        repository.saveApiSettings(updated)
    }

    fun setVerificationMode(mode: VerificationMode) {
        _verificationMode.value = mode
    }

    fun setSampleBitmap(bitmap: Bitmap?) {
        _sampleBitmap.value = bitmap
    }

    fun setReferenceBitmap(bitmap: Bitmap?) {
        _referenceBitmap.value = bitmap
        _selectedReference.value = null
    }

    fun selectReferenceSpecimen(specimen: ReferenceSpecimen) {
        _selectedReference.value = specimen
        viewModelScope.launch {
            val bmp = repository.loadBitmapFromUri(specimen.imageUri)
            _referenceBitmap.value = bmp
        }
    }

    fun setUseExternalApi(use: Boolean) {
        _useExternalApi.value = use
    }

    fun loadPresetDemoSignatures(asli: Boolean) {
        val sample = repository.createSyntheticSignatureBitmap(isGenuine = asli)
        _sampleBitmap.value = sample
        if (_verificationMode.value == VerificationMode.COMPARISON && _referenceBitmap.value == null) {
            val ref = repository.createSyntheticSignatureBitmap(isGenuine = true)
            _referenceBitmap.value = ref
        }
    }

    fun resetVerification() {
        _verificationState.value = VerificationState.Idle
        _sampleBitmap.value = null
        _referenceBitmap.value = null
        _selectedReference.value = null
    }

    fun startVerification(
        documentTitle: String = "Pemeriksaan Dokumen Tanda Tangan",
        signerName: String = "Subjek Tanda Tangan"
    ) {
        val sample = _sampleBitmap.value
        if (sample == null) {
            _verificationState.value = VerificationState.Failed("Silakan masukkan atau gambar tanda tangan sampel terlebih dahulu.")
            return
        }

        val mode = _verificationMode.value
        val ref = _referenceBitmap.value

        if (mode == VerificationMode.COMPARISON && ref == null) {
            _verificationState.value = VerificationState.Failed("Mode komparasi 1:1 membutuhkan tanda tangan referensi/spesimen.")
            return
        }

        viewModelScope.launch {
            _verificationState.value = VerificationState.Processing("Mengekstraksi fitur biometrik & memproses ke Model...")

            try {
                val result = repository.verifySignature(
                    sampleBitmap = sample,
                    referenceBitmap = ref,
                    mode = mode,
                    useExternalApi = _useExternalApi.value
                )

                // Save to internal storage & database
                val sampleUri = repository.saveBitmapToInternalStorage(sample, "sample")
                val refUri = ref?.let { repository.saveBitmapToInternalStorage(it, "reference") }

                val record = VerificationRecord(
                    documentTitle = documentTitle.ifBlank { "Verifikasi #${System.currentTimeMillis() % 10000}" },
                    signerName = signerName.ifBlank { "Anonim" },
                    verdict = result.verdict,
                    confidence = result.confidence,
                    mode = mode,
                    sampleImageUri = sampleUri,
                    referenceImageUri = refUri,
                    apiSource = result.sourceLabel,
                    latencyMs = result.latencyMs,
                    notes = result.rawResponse ?: "",
                    strokeContinuity = result.biometrics.strokeContinuity,
                    aspectProportionMatch = result.biometrics.aspectProportionMatch,
                    pixelDensityConsistency = result.biometrics.pixelDensityConsistency,
                    baselineSlope = result.biometrics.baselineSlope,
                    structuralSimilarity = result.biometrics.structuralSimilarity,
                    smoothnessScore = result.biometrics.smoothnessScore
                )
                repository.saveRecord(record)

                _verificationState.value = VerificationState.Completed(result)
            } catch (e: Exception) {
                _verificationState.value = VerificationState.Failed("Terjadi kesalahan proses: ${e.localizedMessage ?: "Unknown"}")
            }
        }
    }

    fun updateApiSettings(settings: ApiSettings) {
        _apiSettings.value = settings
        repository.saveApiSettings(settings)
    }

    fun testApiConnection() {
        viewModelScope.launch {
            _isTestingApi.value = true
            _apiTestStatus.value = "Menghubungi endpoint..."
            when (val res = repository.testApiConnection()) {
                is ApiResponse.Success -> {
                    _apiTestStatus.value = "SUKSES: ${res.data} (${res.latencyMs} ms)"
                }
                is ApiResponse.Error -> {
                    _apiTestStatus.value = "GAGAL: ${res.message}"
                }
            }
            _isTestingApi.value = false
        }
    }

    fun loadGeneralUseCase(useCase: com.example.data.model.GeneralUseCase) {
        val sample = repository.createSyntheticSignatureBitmap(isGenuine = true)
        val ref = repository.createSyntheticSignatureBitmap(isGenuine = true)
        _sampleBitmap.value = sample
        _referenceBitmap.value = ref
        _verificationMode.value = VerificationMode.COMPARISON
    }

    fun getPracticalAdvice(
        verdict: VerificationVerdict,
        confidence: Float,
        profile: com.example.data.model.InspectionProfile
    ): String {
        return if (profile == com.example.data.model.InspectionProfile.GENERAL) {
            when (verdict) {
                VerificationVerdict.ASLI ->
                    "Tanda tangan ini cocok dengan acuan Anda ($confidence%). Karakter tarikan garis konsisten dan wajar untuk keperluan kuitansi, surat izin, atau tanda terima paket."
                VerificationVerdict.PALSU ->
                    "Peringatan: Pola goresan tanda tangan ini sangat berbeda jauh ($confidence%) dari acuan Anda. Sebaiknya konfirmasi langsung kepada penandatangan sebelum menerima transaksi atau dokumen."
                VerificationVerdict.MENCURIGAKAN ->
                    "Terdapat kemiripan umum ($confidence%), namun ada beberapa sudut atau kelenturan goresan yang sedikit berbeda. Bila untuk urusan bank atau dokumen resmi, mintalah tanda tangan ulang yang lebih jelas."
            }
        } else {
            when (verdict) {
                VerificationVerdict.ASLI ->
                    "Hasil evaluasi forensik memenuhi kriteria otentisitas tingkat tinggi ($confidence%). Proporsi aspek geometris, indeks kelenturan kontinuitas, dan distribusi piksel identik dengan spesimen kontrol sah."
                VerificationVerdict.PALSU ->
                    "Terdeteksi anomali goresan struktural ($confidence%). Terdapat indikasi keraguan tarikan (hesitation tremor) dan ketidakcocokan baseline kemiringan yang signifikan terhadap spesimen legal."
                VerificationVerdict.MENCURIGAKAN ->
                    "Kesesuaian berada di zona ambang kritis ($confidence%). Diperlukan pemeriksaan mikroskopik fisik dokumen asli atau spesimen pembanding tambahan untuk kepastian legal formal."
            }
        }
    }

    fun addReferenceSpecimen(
        name: String,
        role: String,
        bitmap: Bitmap,
        notes: String = "",
        category: String = "Umum"
    ) {
        viewModelScope.launch {
            val uri = repository.saveBitmapToInternalStorage(bitmap, "specimen")
            repository.saveSpecimen(
                ReferenceSpecimen(
                    name = name,
                    roleOrTitle = role,
                    imageUri = uri,
                    notes = notes,
                    category = category
                )
            )
        }
    }

    fun deleteSpecimen(specimen: ReferenceSpecimen) {
        viewModelScope.launch {
            repository.deleteSpecimen(specimen)
        }
    }

    fun deleteRecord(record: VerificationRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllRecords()
        }
    }

    fun setHistoryFilter(verdict: VerificationVerdict?) {
        _historyFilter.value = verdict
    }

    fun setHistorySearchQuery(query: String) {
        _historySearchQuery.value = query
    }
}
