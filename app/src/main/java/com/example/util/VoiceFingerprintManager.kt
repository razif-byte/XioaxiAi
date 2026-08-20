package com.example.util

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sqrt

class VoiceFingerprintManager {

    private val _waveformState = MutableStateFlow(List(30) { 0.1f })
    val waveformState = _waveformState.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var recordJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    // Pre-calculated features representing Razif's default blueprint voice characteristics
    // In case enrollment hasn't run yet, these are the typical features of male speaker "Razif" (frequency ~100-150Hz etc.)
    private val defaultRazifSignature = listOf(0.18f, 120.0f, 0.45f, 0.72f)

    companion object {
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val TAG = "VoiceFingerprint"
    }

    /**
     * Compute a 4-dimensional biometric voice fingerprint vector from a raw PCM PCM16 signal:
     * 1. Energy factor (RMS amplitude ratio)
     * 2. Estimated Pitch/Frequency using Zero Crossing Rate (ZCR)
     * 3. Signal Variance (rough estimate of phonetic dynamic range)
     * 4. Spectral Crest factor estimate (crest/peak amplitude ratio)
     */
    fun computeFingerprint(pcmData: ShortArray, size: Int): List<Float> {
        if (size <= 0) return listOf(0f, 0f, 0f, 0f)

        var sumSquare = 0.0
        var zeroCrossings = 0
        var maxVal = 0

        for (i in 0 until size) {
            val sample = pcmData[i].toInt()
            sumSquare += sample * sample
            if (abs(sample) > maxVal) {
                maxVal = abs(sample)
            }
            if (i > 0) {
                // Zero crossing check
                if ((pcmData[i] >= 0 && pcmData[i - 1] < 0) || (pcmData[i] < 0 && pcmData[i - 1] >= 0)) {
                    zeroCrossings++
                }
            }
        }

        val rms = sqrt(sumSquare / size).toFloat()
        val normalizedEnergy = (rms / 32768.0f).coerceIn(0f, 1f)
        
        // Zero crossing rate to frequency mapping
        val zcr = (zeroCrossings.toFloat() / (size.toFloat() / SAMPLE_RATE)) / 2.0f
        val estimatedPitchHz = zcr.coerceIn(80f, 350f) // Typical human pitch range

        // Variance
        var varianceSum = 0.0
        val mean = pcmData.take(size).map { it.toFloat() }.average()
        for (i in 0 until size) {
            val diff = pcmData[i] - mean
            varianceSum += diff * diff
        }
        val variance = sqrt(varianceSum / size).toFloat() / 32768.0f

        // Crest factor ratio
        val crestFactor = if (rms > 0) (maxVal.toFloat() / rms) else 1f
        val crestRatio = (crestFactor / 10f).coerceIn(0f, 1f)

        return listOf(normalizedEnergy, estimatedPitchHz, variance, crestRatio)
    }

    /**
     * Compare a test fingerprint against the registered biometric fingerprint
     * Returns similarity score from 0.0f (no match) to 1.0f (exact match)
     */
    fun calculateSimilarity(signatureA: List<Float>, signatureB: List<Float>): Float {
        if (signatureA.size < 4 || signatureB.size < 4) return 0f

        // Normalize parameters for comparison
        // Index 0: Energy (0 to 1)
        // Index 1: Pitch (80 to 350) - normalize to 0 to 1
        // Index 2: Variance (0 to 1)
        // Index 3: Crest (0 to 1)

        val pitchA = (signatureA[1] - 80f) / 270f
        val pitchB = (signatureB[1] - 80f) / 270f

        val d0 = abs(signatureA[0] - signatureB[0])
        val d1 = abs(pitchA - pitchB)
        val d2 = abs(signatureA[2] - signatureB[2])
        val d3 = abs(signatureA[3] - signatureB[3])

        // Weight parameters: Pitch and crest ratio are most identity-revealing (0.45 each, energy is only 0.1)
        val weightedDistance = (d0 * 0.1f) + (d1 * 0.5f) + (d2 * 0.1f) + (d3 * 0.3f)
        val similarity = 1.0f - weightedDistance

        return similarity.coerceIn(0.0f, 1.0f)
    }

    /**
     * Serialize signature vector to comma-separated string
     */
    fun serializeSignature(signature: List<Float>): String {
        return signature.joinToString(",")
    }

    /**
     * Deserialize signature vector from comma-separated string
     */
    fun deserializeSignature(signatureStr: String): List<Float> {
        if (signatureStr.isEmpty()) return defaultRazifSignature
        return try {
            signatureStr.split(",").map { it.toFloat() }
        } catch (e: Exception) {
            defaultRazifSignature
        }
    }

    @SuppressLint("MissingPermission")
    fun startLiveAnalysis(onFrameProcessed: (List<Float>) -> Unit) {
        if (_isRecording.value) return

        val minBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        if (minBufSize == AudioRecord.ERROR || minBufSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid buffer size")
            return
        }

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                minBufSize * 2
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord state not initialized")
                return
            }

            audioRecord?.startRecording()
            _isRecording.value = true

            recordJob = scope.launch {
                val buffer = ShortArray(1024)
                val visualizerBuffer = ArrayList<Float>()

                while (_isRecording.value) {
                    val readResult = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (readResult > 0) {
                        // Process fingerprint vector for current frame
                        val signature = computeFingerprint(buffer, readResult)
                        onFrameProcessed(signature)

                        // Generate wave aesthetics for visualizer
                        var sum = 0f
                        for (i in 0 until readResult) {
                            sum += abs(buffer[i].toFloat())
                        }
                        val avg = sum / readResult
                        val normalizedVal = (avg / 32768.0f) * 12.0f // Scale for dynamic waveform
                        val level = normalizedVal.coerceIn(0.05f, 1.0f)

                        visualizerBuffer.add(level)
                        if (visualizerBuffer.size > 30) {
                            visualizerBuffer.removeAt(0)
                        }

                        _waveformState.value = ArrayList(visualizerBuffer)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting voice record: ${e.message}")
            stopAnalysis()
        }
    }

    fun stopAnalysis() {
        _isRecording.value = false
        recordJob?.cancel()
        recordJob = null
        try {
            if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord?.stop()
            }
            audioRecord?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recorder: ${e.message}")
        }
        audioRecord = null
    }
}
