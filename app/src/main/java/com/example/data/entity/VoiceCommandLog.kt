package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_command_logs")
data class VoiceCommandLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val command: String,
    val parsedAction: String,
    val status: String, // "SUKSES", "AKSES DITOLAK (BUKAN RAZIF)", "TIDAK DIKENALI"
    val timestamp: Long = System.currentTimeMillis()
)
