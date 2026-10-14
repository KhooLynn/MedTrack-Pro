package com.lynn.s34598901.medtrack.data.medications

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for medication records
 */
@Dao
interface MedicationDao {

    // Insert one medication record and replace it if the same primary key already exists
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication)

    // Get all medications for one patient, ordered alphabetically by medication name
    @Query("SELECT * FROM medications WHERE patientID = :patientID ORDER BY name ASC")
    fun getMedicationsByPatientID(patientID: String): Flow<List<Medication>>

    // Update whether one medication has been taken and store the date it was marked
    @Query("UPDATE medications SET isTaken = :isTaken, dateTaken = :dateTaken WHERE medicationId = :medicationId")
    suspend fun updateStatus(
        medicationId: Int,
        isTaken: Boolean,
        dateTaken: String
    )

    // Reset medications that were marked as taken on a previous date
    @Query("UPDATE medications SET isTaken = 0, dateTaken = '' WHERE dateTaken != :today")
    suspend fun resetTakenStatus(today: String)
}
