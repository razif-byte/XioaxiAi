package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.VoiceCommandLog
import com.example.data.entity.VoiceProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceDao {
    @Query("SELECT * FROM voice_command_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<VoiceCommandLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: VoiceCommandLog)

    @Query("DELETE FROM voice_command_logs")
    suspend fun clearLogs()

    @Query("SELECT * FROM voice_profiles WHERE id = 1 LIMIT 1")
    fun getVoiceProfileFlow(): Flow<VoiceProfile?>

    @Query("SELECT * FROM voice_profiles WHERE id = 1 LIMIT 1")
    suspend fun getVoiceProfileSync(): VoiceProfile?

    @Query("SELECT * FROM voice_profiles ORDER BY id ASC")
    fun getAllVoiceProfilesFlow(): Flow<List<VoiceProfile>>

    @Query("SELECT * FROM voice_profiles ORDER BY id ASC")
    suspend fun getAllVoiceProfilesSync(): List<VoiceProfile>

    @Query("SELECT * FROM voice_profiles WHERE id = :id LIMIT 1")
    suspend fun getVoiceProfileById(id: Int): VoiceProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceProfile(profile: VoiceProfile)

    @Query("DELETE FROM voice_profiles WHERE id = :id")
    suspend fun deleteVoiceProfileById(id: Int)
}
