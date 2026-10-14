package com.lynn.s34598901.medtrack.data.symptoms

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for symptom records
 */
@Dao
interface SymptomDao {

    // Insert one symptom record and replace it if the same primary key already exists
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptom(symptom: Symptom)

    // Get all symptoms logged for one patient
    @Query("SELECT * FROM symptoms WHERE patientID = :patientID ORDER BY dateTime DESC")
    fun getSymptomsByPatientID(patientID: String): Flow<List<Symptom>>
}