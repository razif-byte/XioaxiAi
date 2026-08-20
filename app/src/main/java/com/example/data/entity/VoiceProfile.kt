package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_profiles")
data class VoiceProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "Razif",
    val isEnrolled: Boolean = false,
    val averagePitch: Float = 0f,
    val spectralSignature: String = "", // Comma-separated float peak ratios (e.g., "0.12,0.45,0.78,0.33")
    val strictVerification: Boolean = false
)
