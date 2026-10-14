package com.lynn.s34598901.medtrack.data.medcoach

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MedCoachTipsDao {

    @Insert
    suspend fun insertTip(tip: MedCoachGenAITips)

    @Query("SELECT * FROM medcoach_genai_tips WHERE patientId = :patientId ORDER BY timestamp DESC")
    fun getTipsForPatient(patientId: String): Flow<List<MedCoachGenAITips>>
}