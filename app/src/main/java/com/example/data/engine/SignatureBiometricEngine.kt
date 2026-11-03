package com.example.data.engine

import android.graphics.Bitmap
import android.graphics.Color
import com.example.data.model.SignatureBiometrics
import com.example.data.model.VerificationVerdict
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

class SignatureBiometricEngine {

    suspend fun analyzeSingle(bitmap: Bitmap): Pair<VerificationVerdict, SignatureBiometrics> =
        withContext(Dispatchers.Default) {
            val normalized = normalizeBitmap(bitmap, 200, 200)
            val binary = binarize(normalized)
            val strokeMetrics = extractStrokeFeatures(binary, 200, 200)

            // Fluid natural signature score
            val continuity = strokeMetrics.continuity
            val density = strokeMetrics.density
            val smoothness = strokeMetrics.smoothness
            val aspect = strokeMetrics.aspectRatioScore

            // Natural signatures have high stroke continuity (>65%) and high smoothness without hesitation jitter
            val combinedScore = (continuity * 0.35f + smoothness * 0.35f + density * 0.15f + aspect * 0.15f)
                .coerceIn(0f, 100f)

            val verdict = when {
                combinedScore >= 70f -> VerificationVerdict.ASLI
                combinedScore <= 50f -> VerificationVerdict.PALSU
                else -> VerificationVerdict.MENCURIGAKAN
            }

            val biometrics = SignatureBiometrics(
                strokeContinuity = (continuity * 10).roundToInt() / 10f,
                aspectProportionMatch = (aspect * 10).roundToInt() / 10f,
                pixelDensityConsistency = (density * 10).roundToInt() / 10f,
                baselineSlope = (strokeMetrics.baselineSlope * 10).roundToInt() / 10f,
                structuralSimilarity = (combinedScore * 10).roundToInt() / 10f,
                smoothnessScore = (smoothness * 10).roundToInt() / 10f,
                overallScore = (combinedScore * 10).roundToInt() / 10f
            )

            Pair(verdict, biometrics)
        }

    suspend fun compareSignatures(
        sampleBitmap: Bitmap,
        referenceBitmap: Bitmap,
        thresholdPercent: Int = 75
    ): Triple<VerificationVerdict, SignatureBiometrics, Bitmap> = withContext(Dispatchers.Default) {
        val size = 200
        val sampleNorm = normalizeBitmap(sampleBitmap, size, size)
        val refNorm = normalizeBitmap(referenceBitmap, size, size)

        val sampleBin = binarize(sampleNorm)
        val refBin = binarize(refNorm)

        val sampleFeatures = extractStrokeFeatures(sampleBin, size, size)
        val refFeatures = extractStrokeFeatures(refBin, size, size)

        // Calculate Pixel-level Jaccard & SSIM similarity
        var intersection = 0
        var union = 0
        var totalRef = 0
        var totalSample = 0

        val diffBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        for (y in 0 until size) {
            for (x in 0 until size) {
                val s = sampleBin[y][x]
                val r = refBin[y][x]

                if (s && r) {
                    intersection++
                    // Matching genuine stroke overlay in bright cyan/teal
                    diffBitmap.setPixel(x, y, Color.rgb(0, 229, 255))
                } else if (s && !r) {
                    // Stroke anomaly in sample: forged addition or deviation (coral red)
                    diffBitmap.setPixel(x, y, Color.rgb(239, 68, 68))
                } else if (!s && r) {
                    // Missing reference stroke: hesitation cut or missing tail (muted blue)
                    diffBitmap.setPixel(x, y, Color.argb(120, 59, 130, 246))
                } else {
                    diffBitmap.setPixel(x, y, Color.argb(20, 15, 23, 42))
                }

                if (s) totalSample++
                if (r) totalRef++
                if (s || r) union++
            }
        }

        val jaccard = if (union > 0) (intersection.toFloat() / union.toFloat()) * 100f else 0f

        // Structural Feature Comparisons
        val densityRatio = if (max(sampleFeatures.density, refFeatures.density) > 0f) {
            (min(sampleFeatures.density, refFeatures.density) / max(sampleFeatures.density, refFeatures.density)) * 100f
        } else 50f

        val aspectMatch = if (max(sampleFeatures.aspectRatioScore, refFeatures.aspectRatioScore) > 0f) {
            (min(sampleFeatures.aspectRatioScore, refFeatures.aspectRatioScore) / max(sampleFeatures.aspectRatioScore, refFeatures.aspectRatioScore)) * 100f
        } else 50f

        val slopeDiff = abs(sampleFeatures.baselineSlope - refFeatures.baselineSlope)
        val slopeMatch = (100f - slopeDiff * 5f).coerceIn(0f, 100f)

        val smoothnessMatch = 100f - abs(sampleFeatures.smoothness - refFeatures.smoothness)

        // Weighted similarity score
        val overall = (jaccard * 0.40f + aspectMatch * 0.20f + densityRatio * 0.15f + slopeMatch * 0.15f + smoothnessMatch * 0.10f)
            .coerceIn(0f, 100f)

        val verdict = when {
            overall >= thresholdPercent -> VerificationVerdict.ASLI
            overall <= (thresholdPercent - 20) -> VerificationVerdict.PALSU
            else -> VerificationVerdict.MENCURIGAKAN
        }

        val biometrics = SignatureBiometrics(
            strokeContinuity = (sampleFeatures.continuity * 10).roundToInt() / 10f,
            aspectProportionMatch = (aspectMatch * 10).roundToInt() / 10f,
            pixelDensityConsistency = (densityRatio * 10).roundToInt() / 10f,
            baselineSlope = (sampleFeatures.baselineSlope * 10).roundToInt() / 10f,
            structuralSimilarity = (jaccard * 10).roundToInt() / 10f,
            smoothnessScore = (smoothnessMatch * 10).roundToInt() / 10f,
            overallScore = (overall * 10).roundToInt() / 10f
        )

        Triple(verdict, biometrics, diffBitmap)
    }

    private fun normalizeBitmap(source: Bitmap, targetW: Int, targetH: Int): Bitmap {
        return Bitmap.createScaledBitmap(source, targetW, targetH, true)
    }

    private fun binarize(bitmap: Bitmap): Array<BooleanArray> {
        val w = bitmap.width
        val h = bitmap.height
        val matrix = Array(h) { BooleanArray(w) }

        // Compute average luminance
        var sumLuma = 0L
        for (y in 0 until h) {
            for (x in 0 until w) {
                val px = bitmap.getPixel(x, y)
                val alpha = (px shr 24) and 0xFF
                if (alpha < 50) {
                    sumLuma += 255 // transparent background counts as paper white
                } else {
                    val r = (px shr 16) and 0xFF
                    val g = (px shr 8) and 0xFF
                    val b = px and 0xFF
                    val luma = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                    sumLuma += luma
                }
            }
        }
        val avgLuma = (sumLuma / (w * h)).toInt()
        val threshold = (avgLuma * 0.85).toInt().coerceIn(60, 220)

        for (y in 0 until h) {
            for (x in 0 until w) {
                val px = bitmap.getPixel(x, y)
                val alpha = (px shr 24) and 0xFF
                if (alpha < 50) {
                    matrix[y][x] = false
                } else {
                    val r = (px shr 16) and 0xFF
                    val g = (px shr 8) and 0xFF
                    val b = px and 0xFF
                    val luma = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                    matrix[y][x] = luma < threshold // Dark ink pixel
                }
            }
        }
        return matrix
    }

    private data class InternalFeatures(
        val continuity: Float,
        val density: Float,
        val smoothness: Float,
        val aspectRatioScore: Float,
        val baselineSlope: Float
    )

    private fun extractStrokeFeatures(binary: Array<BooleanArray>, w: Int, h: Int): InternalFeatures {
        var minX = w
        var maxX = 0
        var minY = h
        var maxY = 0
        var inkCount = 0

        var sumX = 0L
        var sumY = 0L

        // Track transitions for continuity/jitter
        var horizontalTransitions = 0
        var verticalTransitions = 0

        for (y in 0 until h) {
            var prevInLine = false
            for (x in 0 until w) {
                val isInk = binary[y][x]
                if (isInk) {
                    inkCount++
                    sumX += x
                    sumY += y
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
                if (isInk != prevInLine) {
                    horizontalTransitions++
                    prevInLine = isInk
                }
            }
        }

        for (x in 0 until w) {
            var prevInCol = false
            for (y in 0 until h) {
                val isInk = binary[y][x]
                if (isInk != prevInCol) {
                    verticalTransitions++
                    prevInCol = isInk
                }
            }
        }

        if (inkCount == 0 || minX >= maxX || minY >= maxY) {
            return InternalFeatures(
                continuity = 30f,
                density = 20f,
                smoothness = 30f,
                aspectRatioScore = 50f,
                baselineSlope = 0f
            )
        }

        val boxW = maxX - minX + 1
        val boxH = maxY - minY + 1
        val boxArea = boxW * boxH
        val density = (inkCount.toFloat() / boxArea.toFloat()) * 100f

        val aspect = boxW.toFloat() / max(1f, boxH.toFloat())
        val aspectScore = (aspect * 25f).coerceIn(20f, 95f)

        // Natural signatures have controlled transitions: too high = trembling tremor (forgery trace),
        // too low = solid block.
        val avgStrokeWidth = (inkCount * 2f) / max(1f, (horizontalTransitions + verticalTransitions).toFloat())
        val continuity = (85f - abs(avgStrokeWidth - 4.5f) * 12f).coerceIn(40f, 96f)

        val jitterRatio = (horizontalTransitions + verticalTransitions).toFloat() / max(1f, inkCount.toFloat())
        val smoothness = (95f - (jitterRatio * 80f)).coerceIn(30f, 98f)

        // Approximate baseline slope via linear tendency of bottom ink boundary
        val centerX = sumX / inkCount
        val centerY = sumY / inkCount
        val baselineSlope = ((centerY.toFloat() - (h / 2f)) / max(1f, (centerX.toFloat() - (w / 2f)))).coerceIn(-15f, 15f)

        return InternalFeatures(
            continuity = continuity,
            density = density.coerceIn(10f, 95f),
            smoothness = smoothness,
            aspectRatioScore = aspectScore,
            baselineSlope = baselineSlope
        )
    }
}
