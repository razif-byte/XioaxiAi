package com.example.data.repository

import com.example.data.dao.VoiceDao
import com.example.data.entity.VoiceCommandLog
import com.example.data.entity.VoiceProfile
import kotlinx.coroutines.flow.Flow

class VoiceRepository(private val voiceDao: VoiceDao) {
    val allLogs: Flow<List<VoiceCommandLog>> = voiceDao.getAllLogs()
    val voiceProfile: Flow<VoiceProfile?> = voiceDao.getVoiceProfileFlow()
    val allVoiceProfiles: Flow<List<VoiceProfile>> = voiceDao.getAllVoiceProfilesFlow()

    suspend fun insertLog(log: VoiceCommandLog) {
        voiceDao.insertLog(log)
    }

    suspend fun clearLogs() {
        voiceDao.clearLogs()
    }

    suspend fun getVoiceProfileSync(): VoiceProfile? {
        return voiceDao.getVoiceProfileSync()
    }

    suspend fun getAllVoiceProfilesSync(): List<VoiceProfile> {
        return voiceDao.getAllVoiceProfilesSync()
    }

    suspend fun getVoiceProfileById(id: Int): VoiceProfile? {
        return voiceDao.getVoiceProfileById(id)
    }

    suspend fun saveVoiceProfile(profile: VoiceProfile) {
        voiceDao.insertVoiceProfile(profile)
    }

    suspend fun deleteVoiceProfileById(id: Int) {
        voiceDao.deleteVoiceProfileById(id)
    }
}
